package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	private final CandleMinuteRepository candleMinuteRepository;
	private final CandleDayRepository candleDayRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final CandleSchedulerService candleSchedulerService;
	private final CandleCacheService candleCacheService;
	private final WebSocketService webSocketService;

	public List<CandleDTO> getCandle(CandleType type, String stockCode, String startTime, String endTime) {
		LocalDateTime from = parseStartTime(startTime);
		LocalDateTime to = parseEndTime(endTime);

		if (type.isMinuteType()) {
			return getMinuteCandles(stockCode, from, to, type.getMinute());
		}

		return switch (type) {
		case DAY -> getDayCandles(stockCode, from, to);
		default -> throw new IllegalArgumentException("지원하지 않는 타입: " + type);
		};
	}

	private List<CandleDTO> getMinuteCandles(String stockCode, LocalDateTime from, LocalDateTime to, int minute) {
		// 🎯 캐시 조회 시 엔티티가 아닌 CandleWithMA 형태로 가져옴
		List<CandleWithMA<CandleMinute>> wrappedCache = getCachedMinuteCandlesInRange(stockCode, minute, from, to);
		List<CandleDTO> result;

		if (wrappedCache.isEmpty()) {
			// DB 조회 시에는 이평선이 없으므로 순수 엔티티 리스트를 획득
			List<CandleMinute> dbCandles = findMinuteCandlesFromDb(stockCode, from, to, minute);
			// 💡 DB에서 온 데이터들은 무거운 실시간 계산 대신 우선 빈 이평선 맵(또는 null)으로 감싸 DTO로 변환
			result = new ArrayList<>(
					dbCandles.stream().map(c -> CandleDTO.from(new CandleWithMA<>(c, Map.of()))).toList());
		} else {
			// 캐시 데이터가 유효하면 이평선 정보가 포함된 통째로 DTO 변환
			result = new ArrayList<>(wrappedCache.stream().map(CandleDTO::from).toList());
		}

		addCurrentCandleIfNewer(result, stockCode);
		return result;
	}

	private List<CandleDTO> getDayCandles(String stockCode, LocalDateTime from, LocalDateTime to) {
		LocalDate fromDate = from.toLocalDate();
		LocalDate toDate = to.toLocalDate();

		// 🎯 캐시 모델 정상화
		List<CandleWithMA<CandleDay>> wrappedCache = getCachedDayCandlesInRange(stockCode, fromDate, toDate);
		List<CandleDTO> result;

		if (wrappedCache.isEmpty()) {
			List<CandleDay> days = candleDayRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode, fromDate,
					toDate);
			result = new ArrayList<>(days.stream().map(d -> CandleDTO.from(new CandleWithMA<>(d, Map.of()))).toList());
		} else {
			result = new ArrayList<>(wrappedCache.stream().map(CandleDTO::from).toList());
		}

		CandleDTO todayCandle = getTodayCandle(stockCode);
		addCandleIfNewer(result, todayCandle);

		return result;
	}

	// 🎯 반환 타입을 List<CandleWithMA<CandleMinute>> 구조로 교정
	private List<CandleWithMA<CandleMinute>> getCachedMinuteCandlesInRange(String stockCode, int minute,
			LocalDateTime from,
			LocalDateTime to) {
		List<CandleWithMA<CandleMinute>> cache = getCachedMinuteCandles(stockCode, minute);

		if (!isMinuteCacheCoveringRange(cache, from, to)) {
			return List.of();
		}

		return cache.stream().filter(c -> isBetween(c.getCandle().getTime(), from, to)).toList();
	}

	// 🎯 반환 타입 교정
	private List<CandleWithMA<CandleMinute>> getCachedMinuteCandles(String stockCode, int minute) {
		return switch (minute) {
		case 1 -> candleCacheService.getOneMinCandles(stockCode);
		case 5 -> candleCacheService.getFiveMinCandles(stockCode);
		default -> List.of();
		};
	}

	// 🎯 제네릭 <T> 제거하고 CandleMinute를 명시적으로 선언하도록 변경
	private boolean isMinuteCacheCoveringRange(List<CandleWithMA<CandleMinute>> cache, LocalDateTime from, LocalDateTime to) {
		if (cache == null || cache.isEmpty()) {
			return false;
		}

		// 💡 명확한 타입 덕분에 캐스팅(instanceof) 없이 바로 .getTime() 호출이 가능해집니다!
		LocalDateTime cacheStart = cache.get(0).getCandle().getTime();
		LocalDateTime cacheEnd = cache.get(cache.size() - 1).getCandle().getTime();

		return !from.isBefore(cacheStart) && !to.isAfter(cacheEnd);
	}

	private List<CandleMinute> findMinuteCandlesFromDb(String stockCode, LocalDateTime from, LocalDateTime to,
			int minute) {
		List<CandleMinute> oneMinCandles = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
				from, to);

		if (minute == 1) {
			return oneMinCandles;
		}

		return groupMinuteCandles(oneMinCandles, minute);
	}

	private List<CandleMinute> groupMinuteCandles(List<CandleMinute> candles, int minute) {
		return new ArrayList<>(candles.stream().collect(Collectors.groupingBy(c -> floorTime(c.getTime(), minute)))
				.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.map(entry -> toGroupedCandleMinute(entry.getKey(), entry.getValue())).toList());
	}

	private CandleMinute toGroupedCandleMinute(LocalDateTime time, List<CandleMinute> group) {
		group.sort(Comparator.comparing(CandleMinute::getTime));

		CandleMinute first = group.get(0);
		CandleMinute last = group.get(group.size() - 1);

		int high = group.stream().mapToInt(CandleMinute::getHigh).max().orElse(first.getHigh());
		int low = group.stream().mapToInt(CandleMinute::getLow).min().orElse(first.getLow());
		long sellQty = group.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long buyQty = group.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
		long tradeAmount = group.stream().mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0L).sum();

		return new CandleMinute(null, first.getStockCode(), time, first.getOpen(), high, low, last.getClose(), buyQty,
				sellQty, buyQty + sellQty, tradeAmount);
	}

	private LocalDateTime floorTime(LocalDateTime time, int minute) {
		return time.withMinute((time.getMinute() / minute) * minute).withSecond(0).withNano(0);
	}



	// 🎯 반환 타입 교정
	private List<CandleWithMA<CandleDay>> getCachedDayCandlesInRange(String stockCode, LocalDate from, LocalDate to) {
		List<CandleWithMA<CandleDay>> cache = candleCacheService.getDayCandles(stockCode);

		if (!isDayCacheCoveringRange(cache, from, to)) {
			return List.of();
		}

		return cache.stream()
				.filter(c -> !c.getCandle().getDate().isBefore(from) && !c.getCandle().getDate().isAfter(to)).toList();
	}

	// 🎯 타입 바인딩 교정
	private boolean isDayCacheCoveringRange(List<CandleWithMA<CandleDay>> cache, LocalDate from, LocalDate to) {
		if (cache == null || cache.isEmpty()) {
			return false;
		}

		LocalDate cacheStart = cache.get(0).getCandle().getDate();
		LocalDate cacheEnd = cache.get(cache.size() - 1).getCandle().getDate();

		return !from.isBefore(cacheStart) && !to.isAfter(cacheEnd);
	}

	private boolean isBetween(LocalDateTime time, LocalDateTime from, LocalDateTime to) {
		return !time.isBefore(from) && !time.isAfter(to);
	}

	// 💡 기존의 구형 변환기는 하위 호환을 위해 유지하거나 걷어내도 무방합니다.
//	private List<CandleDTO> toMinuteDTOList(List<CandleMinute> candles) {
//		return new ArrayList<>(candles.stream().map(CandleDTO::from).toList());
//	}
//
//	private List<CandleDTO> toDayDTOList(List<CandleDay> candles) {
//		return new ArrayList<>(candles.stream().map(CandleDTO::from).toList());
//	}

	private void addCurrentCandleIfNewer(List<CandleDTO> result, String stockCode) {
		CandleDTO currentCandle = getCurrentCandleFromRedis(stockCode);
		addCandleIfNewer(result, currentCandle);
	}

	private void addCandleIfNewer(List<CandleDTO> result, CandleDTO candle) {
		if (candle == null) {
			return;
		}

		if (result.isEmpty()) {
			result.add(candle);
			return;
		}

		LocalDateTime lastTime = parseCandleTime(result.get(result.size() - 1).getTime());
		LocalDateTime newTime = parseCandleTime(candle.getTime());

		if (newTime.isAfter(lastTime)) {
			result.add(candle);
		}
	}

	private LocalDateTime parseCandleTime(String time) {
		if (time.length() == 10) {
			return LocalDate.parse(time).atStartOfDay();
		}

		return LocalDateTime.parse(time);
	}

	private CandleDTO getTodayCandle(String stockCode) {
		LocalDate today = LocalDate.now();

		List<CandleMinute> todayMinutes = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
				today.atStartOfDay(), today.plusDays(1).atStartOfDay());

		CandleDTO currentCandle = getCurrentCandleFromRedis(stockCode);

		if (todayMinutes.isEmpty() && currentCandle == null) {
			return null;
		}

		int open = !todayMinutes.isEmpty() ? todayMinutes.get(0).getOpen() : currentCandle.getOpen();
		int high = todayMinutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(open);
		int low = todayMinutes.stream().mapToInt(CandleMinute::getLow).min().orElse(open);
		int close = !todayMinutes.isEmpty() ? todayMinutes.get(todayMinutes.size() - 1).getClose() : open;
		long sellQty = todayMinutes.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long buyQty = todayMinutes.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();

		if (currentCandle != null) {
			high = Math.max(high, currentCandle.getHigh());
			low = Math.min(low, currentCandle.getLow());
			close = currentCandle.getClose();
			sellQty += currentCandle.getSellQty();
			buyQty += currentCandle.getBuyQty();
		}

		return CandleDTO.today(today, open, high, low, close, sellQty, buyQty);
	}

	private CandleDTO getCurrentCandleFromRedis(String stockCode) {
		LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
		String key = "candle:1m:" + stockCode + ":" + now.format(FMT);

		Map<Object, Object> current = redisTemplate.opsForHash().entries(key);
		if (current.isEmpty()) {
			return null;
		}

		try {
			Object open = current.get("open");
			Object high = current.get("high");
			Object low = current.get("low");
			Object close = current.get("close");

			if (open == null || high == null || low == null || close == null) {
				return null;
			}

			return CandleDTO.current(now, Integer.parseInt(String.valueOf(open)),
					Integer.parseInt(String.valueOf(high)), Integer.parseInt(String.valueOf(low)),
					Integer.parseInt(String.valueOf(close)), parseLong(current.get("sellQty")),
					parseLong(current.get("buyQty")));
		} catch (Exception e) {
			log.error("Redis 현재 캔들 조회 실패 - stockCode: {}, error: {}", stockCode, e.getMessage());
			return null;
		}
	}

	private long parseLong(Object value) {
		return value == null ? 0L : Long.parseLong(String.valueOf(value));
	}

	private LocalDateTime parseStartTime(String startTime) {
		return startTime != null ? LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(3);
	}

	private LocalDateTime parseEndTime(String endTime) {
		return endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();
	}

	public void saveCandleOrder(String stockCode, Integer currentPrice, int buyQty, int sellQty, long tradeAmount,
			LocalDateTime lastExecutionTime) {
		CandleDTO candleDTO = candleSchedulerService.saveCurrentCandle(stockCode, currentPrice, buyQty, sellQty,
				tradeAmount, lastExecutionTime);

		webSocketService.sendCurrentCandle(candleDTO, stockCode);
	}
}