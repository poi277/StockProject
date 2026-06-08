package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
			return getMinuteCandles(stockCode, from, to, type);
		}

		return switch (type) {
		case DAY -> getDayCandles(stockCode, from, to);
		default -> throw new IllegalArgumentException("지원하지 않는 타입: " + type);
		};
	}

	public List<CandleDTO> getCandleInit(CandleType type, String stockCode) {
		if (type.isMinuteType()) {
			return getMinuteCandlesInit(stockCode, type);
		}

		return switch (type) {
		case DAY -> getDayCandlesInit(stockCode, type);
		default -> throw new IllegalArgumentException("지원하지 않는 타입: " + type);
		};
	}

	private List<CandleDTO> getMinuteCandlesInit(String stockCode, CandleType type) {
		// 캐시 스토어 전체 조회 (예: 1000개)
		List<CandleWithMA<CandleMinute>> cache = candleCacheService.getCandles(type, stockCode);
		List<CandleWithMA<CandleMinute>> wrappedCache;

		if (cache.isEmpty()) {
			// 시가 비어있다면 DB에서 최신 100개를 긁어옴
			log.info("분봉 초기화 캐시 공백 - 종목: {}. DB에서 최신 100개를 호출합니다.", stockCode);
			List<CandleMinute> dbCandles = candleMinuteRepository.findTop100ByStockCodeOrderByTimeDesc(stockCode);

			// 과거 -> 현재 순서로 정렬
			dbCandles.sort(Comparator.comparing(CandleMinute::getTime));

			wrappedCache = dbCandles.stream().map(c -> new CandleWithMA<>(c, new HashMap<>()))
					.collect(Collectors.toList());
			calculateMovingAveragesInPlace(wrappedCache);
		} else {
			// 캐시가 존재한다면 뒤에서 최대 100개만 안전하게 짤라서 사용
			int cacheSize = cache.size();
			int targetSize = 10; // 초기화 스펙 개수
			wrappedCache = cache.subList(Math.max(0, cacheSize - targetSize), cacheSize);
		}
		if (wrappedCache.isEmpty()) {
			return List.of();
		}
		// 4. DTO 파싱 변환
		List<CandleDTO> result = wrappedCache.stream().map(w -> {
			CandleMinute c = w.getCandle();
			String timeStr = c.getTime() != null ? c.getTime().toString() : "";
			return CandleDTO.of(timeStr, c.getOpen(), c.getHigh(), c.getLow(), c.getClose(), c.getBuyQty(),
					c.getSellQty(), c.getTotalVolume(), c.getTradeAmount(), w.getMa());
		}).collect(Collectors.toList());

		// 5. 실시간 Live 캔들 병합 보장
		addCurrentCandleIfNewer(result, stockCode);
		return result;
	}

	private List<CandleDTO> getDayCandlesInit(String stockCode, CandleType type) {
		List<CandleWithMA<CandleDay>> cache = candleCacheService.getCandles(type, stockCode);
		List<CandleWithMA<CandleDay>> wrappedCache;

		if (cache.isEmpty()) {
			log.info("일봉 초기화 캐시 공백 - 종목: {}. DB에서 최신 일봉 100개를 호출합니다.", stockCode);
			List<CandleDay> days = candleDayRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			days.sort(Comparator.comparing(CandleDay::getDate));
			wrappedCache = days.stream().map(d -> new CandleWithMA<>(d, new HashMap<>())).collect(Collectors.toList());
		} else {
			int cacheSize = cache.size();
			int targetSize = 100;
			wrappedCache = cache.subList(Math.max(0, cacheSize - targetSize), cacheSize);
		}

		if (wrappedCache.isEmpty()) {
			return List.of();
		}
		List<CandleDTO> result = new ArrayList<>(wrappedCache.stream().map(CandleDTO::from).toList());
		CandleDTO todayCandle = getTodayCandle(stockCode);
		addCandleIfNewer(result, todayCandle);

		return result;
	}

	private List<CandleDTO> getMinuteCandles(String stockCode, LocalDateTime from, LocalDateTime to, CandleType type) {
	    List<CandleWithMA<CandleMinute>> wrappedCache = getCachedMinuteCandlesInRange(stockCode, type, from, to);

	    if (wrappedCache.isEmpty()) {
	        List<CandleMinute> dbCandles = findMinuteCandlesFromDb(stockCode, from, to, type.getMinute());

	        wrappedCache = dbCandles.stream()
	            .map(c -> new CandleWithMA<>(c, new HashMap<>()))
	            .collect(Collectors.toList());
	        calculateMovingAveragesInPlace(wrappedCache);
	    }

	    if (wrappedCache.isEmpty()) {
	        return List.of();
	    }

	    log.info("MA 계산 후: {}", wrappedCache.get(0).getMa());

	    List<CandleDTO> result = wrappedCache.stream().map(w -> {
	        CandleMinute c = w.getCandle();
			String timeStr = c.getTime() != null ? c.getTime().toString() : "";
	        return CandleDTO.of(timeStr, c.getOpen(), c.getHigh(), c.getLow(), c.getClose(),
	            c.getBuyQty(), c.getSellQty(), c.getTotalVolume(), c.getTradeAmount(), w.getMa());
	    }).collect(Collectors.toList());

	    addCurrentCandleIfNewer(result, stockCode);
	    return result;
	}

	private List<CandleDTO> getDayCandles(String stockCode, LocalDateTime from, LocalDateTime to) {
		LocalDate fromDate = from.toLocalDate();
		LocalDate toDate = to.toLocalDate();

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


	private void calculateMovingAveragesInPlace(List<CandleWithMA<CandleMinute>> list) {
		int[] periods = { 5, 20, 60 };

		for (int i = 0; i < list.size(); i++) {
			Map<Integer, Double> maMap = list.get(i).getMa();

			for (int period : periods) {
				// 이평선 기간만큼 데이터가 확보되었을 때 계산
				if (i >= period - 1) {
					double sum = 0;
					for (int j = i - period + 1; j <= i; j++) {
						sum += list.get(j).getCandle().getClose();
					}
					double avg = sum / period;
					maMap.put(period, Math.round(avg * 100.0) / 100.0);
				}
				// 데이터가 아직 부족할 때 (누적 평균 처리)
				else {
					double sum = 0;
					for (int j = 0; j <= i; j++) {
						sum += list.get(j).getCandle().getClose();
					}
					double avg = sum / (i + 1);
					maMap.put(period, Math.round(avg * 100.0) / 100.0);
				}
			}
		}
	}

	private List<CandleWithMA<CandleMinute>> getCachedMinuteCandlesInRange(String stockCode, CandleType type,
			LocalDateTime from, LocalDateTime to) {
		List<CandleWithMA<CandleMinute>> cache = candleCacheService.getCandles(type, stockCode);
		log.info("스타트 {} 엔드 {} ", from, to);
		if (!isMinuteCacheCoveringRange(cache, from, to))
		{
			log.info("비어있음");
			return List.of();
		}
		return cache.stream().filter(c -> isBetween(c.getCandle().getTime(), from, to)).toList();
	}

	private boolean isMinuteCacheCoveringRange(List<CandleWithMA<CandleMinute>> cache, LocalDateTime from, LocalDateTime to) {
		if (cache == null || cache.isEmpty())
			return false;
		LocalDateTime cacheStart = cache.get(0).getCandle().getTime();
		LocalDateTime cacheEnd = cache.get(cache.size() - 1).getCandle().getTime();
		log.info("캐시 스타트 {} ", cacheStart);
		log.info("캐시 엔드 {} ", cacheEnd);
		// startTime이 캐시 범위 안에 있으면 OK
		return !from.isBefore(cacheStart) && !from.isAfter(cacheEnd);
	}

	private List<CandleMinute> findMinuteCandlesFromDb(String stockCode, LocalDateTime from, LocalDateTime to,
			int minute) {
		List<CandleMinute> oneMinCandles = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
				from, to);
		if (minute == 1)
			return oneMinCandles;
		return groupMinuteCandles(oneMinCandles, minute);
	}

	private List<CandleMinute> groupMinuteCandles(List<CandleMinute> candles, int minute) {
		return new ArrayList<>(candles.stream().collect(Collectors.groupingBy(c -> floorTime(c.getTime(), minute)))
				.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.map(entry -> toGroupedCandleMinute(entry.getKey(), entry.getValue())).toList());
	}

	private CandleMinute toGroupedCandleMinute(LocalDateTime time, List<CandleMinute> group) {
		List<CandleMinute> sortedGroup = new ArrayList<>(group);
		sortedGroup.sort(Comparator.comparing(CandleMinute::getTime));
		CandleMinute first = sortedGroup.get(0);
		CandleMinute last = sortedGroup.get(sortedGroup.size() - 1);
		int high = sortedGroup.stream().mapToInt(CandleMinute::getHigh).max().orElse(first.getHigh());
		int low = sortedGroup.stream().mapToInt(CandleMinute::getLow).min().orElse(first.getLow());
		long sellQty = sortedGroup.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long buyQty = sortedGroup.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
		long tradeAmount = sortedGroup.stream().mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0L)
				.sum();
		return new CandleMinute(null, first.getStockCode(), time, first.getOpen(), high, low, last.getClose(), buyQty,
				sellQty, buyQty + sellQty, tradeAmount);
	}

	private LocalDateTime floorTime(LocalDateTime time, int minute) {
		return time.withMinute((time.getMinute() / minute) * minute).withSecond(0).withNano(0);
	}

	private List<CandleWithMA<CandleDay>> getCachedDayCandlesInRange(String stockCode, LocalDate from, LocalDate to) {
		List<CandleWithMA<CandleDay>> cache = candleCacheService.getCandles(CandleType.DAY, stockCode);
		if (!isDayCacheCoveringRange(cache, from, to))
			return List.of();
		return cache.stream()
				.filter(c -> !c.getCandle().getDate().isBefore(from) && !c.getCandle().getDate().isAfter(to)).toList();
	}

	private boolean isDayCacheCoveringRange(List<CandleWithMA<CandleDay>> cache, LocalDate from, LocalDate to) {
		if (cache == null || cache.isEmpty())
			return false;
		LocalDate cacheStart = cache.get(0).getCandle().getDate();
		LocalDate cacheEnd = cache.get(cache.size() - 1).getCandle().getDate();
		return !from.isBefore(cacheStart) && !to.isAfter(cacheEnd);
	}

	private boolean isBetween(LocalDateTime time, LocalDateTime from, LocalDateTime to) {
		return !time.isBefore(from) && !time.isAfter(to);
	}

	private void addCurrentCandleIfNewer(List<CandleDTO> result, String stockCode) {
		CandleDTO currentCandle = getCurrentCandleFromRedis(stockCode);
		addCandleIfNewer(result, currentCandle);
	}

	private void addCandleIfNewer(List<CandleDTO> result, CandleDTO candle) {
		if (candle == null)
			return;
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
		if (time.length() == 10)
			return LocalDate.parse(time).atStartOfDay();
		return LocalDateTime.parse(time);
	}

	private CandleDTO getTodayCandle(String stockCode) {
		LocalDate today = LocalDate.now();
		List<CandleMinute> todayMinutes = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
				today.atStartOfDay(), today.plusDays(1).atStartOfDay());
		CandleDTO currentCandle = getCurrentCandleFromRedis(stockCode);
		if (todayMinutes.isEmpty() && currentCandle == null)
			return null;

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
		if (current.isEmpty())
			return null;
		try {
			Object open = current.get("open");
			Object high = current.get("high");
			Object low = current.get("low");
			Object close = current.get("close");
			if (open == null || high == null || low == null || close == null)
				return null;

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