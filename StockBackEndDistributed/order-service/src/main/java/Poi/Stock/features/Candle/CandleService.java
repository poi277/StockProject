package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleHourRepository;
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
	private static final int[] MA_PERIODS = { 5, 20, 60 };

	private final CandleMinuteRepository candleMinuteRepository;
	private final CandleDayRepository candleDayRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final CandleSchedulerService candleSchedulerService;
	private final CandleHourRepository candleHourRepository;
	private final CandleCacheService candleCacheService;
	private final WebSocketService webSocketService;

	// ===================== 통합 메인 엔트리포인트 =====================

	/**
	 * 기간별 캔들 조회 (통합 파이프라인)
	 */
	public List<CandleDTO> getCandle(CandleType type, String stockCode, String startTime, String endTime) {
		LocalDateTime from = parseStartTime(startTime);
		LocalDateTime to = parseEndTime(endTime);

		String fromStr = from.toString();
		List<CandleWithMA<Candle>> wrappedCache = getCachedCandlesInRange(type, stockCode, fromStr,
				c -> isBetweenStr(c.getCandleTime(), fromStr, to.toString()));

		List<CandleDTO> result;
		if (wrappedCache.isEmpty()) {
			List<Candle> dbCandles = loadRawCandlesFromDb(type, stockCode, from, to);
			List<CandleWithMA<Candle>> wrapped = wrapWithEmptyMA(dbCandles);
			calculateMovingAveragesInPlace(wrapped);
			result = toDTOList(wrapped);
		} else {
			result = toDTOList(wrappedCache);
		}

		appendLatestRealtimeCandle(type, result, stockCode);
		return result;
	}

	/**
	 * 초기 캔들 조회 (Top N 통합)
	 */
	public List<CandleDTO> getCandleInit(CandleType type, String stockCode) {
		int targetSize = type.isMinuteType() ? 10 : 100;

		List<CandleWithMA<Candle>> wrappedCache = candleCacheService.getCandles(type, stockCode);

		if (wrappedCache.isEmpty()) {
			log.info("{} 초기화 캐시 공백 - 종목: {}. DB에서 최신 데이터를 호출합니다.", type, stockCode);
			List<Candle> dbCandles = loadTop100FromDb(type, stockCode);
			wrappedCache = wrapWithEmptyMA(dbCandles);
			calculateMovingAveragesInPlace(wrappedCache);
		} else {
			int cacheSize = wrappedCache.size();
			wrappedCache = wrappedCache.subList(Math.max(0, cacheSize - targetSize), cacheSize);
		}

		if (wrappedCache.isEmpty())
			return List.of();

		List<CandleDTO> result = toDTOList(wrappedCache);
		appendLatestRealtimeCandle(type, result, stockCode);
		return result;
	}


	private List<Candle> loadRawCandlesFromDb(CandleType type, String stockCode, LocalDateTime from, LocalDateTime to) {
		if (type.isMinuteType()) {
			List<CandleMinute> oneMinCandles = candleMinuteRepository
					.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, from, to);

			if (type.getMinute() == 1) {
				return new ArrayList<>(oneMinCandles);
			}
			return new ArrayList<>(groupMinuteCandles(oneMinCandles, type.getMinute()));
		}

		if (type.isHourType()) {
			List<CandleHour> oneHourCandles = candleHourRepository
					.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, from, to);

			if (type.getHourGroup() == 1) {
				return new ArrayList<>(oneHourCandles);
			}
			return new ArrayList<>(groupHourCandles(oneHourCandles, type.getHourGroup()));
		}

		if (type == CandleType.DAY) {
			List<CandleDay> days = candleDayRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
					from.toLocalDate(), to.toLocalDate());
			return new ArrayList<>(days);
		}
		throw new IllegalArgumentException("지원하지 않는 타입: " + type);
	}

	private List<Candle> loadTop100FromDb(CandleType type, String stockCode) {
		if (type.isMinuteType()) {
			List<CandleMinute> dbCandles = candleMinuteRepository.findTop100ByStockCodeOrderByTimeDesc(stockCode);
			dbCandles.sort(Comparator.comparing(CandleMinute::getTime));
			return new ArrayList<>(dbCandles);
		}

		if (type.isHourType()) {
			List<CandleHour> hours = candleHourRepository.findTop100ByStockCodeOrderByTimeDesc(stockCode);
			hours.sort(Comparator.comparing(CandleHour::getTime));
			if (type.getHourGroup() == 1) {
				return new ArrayList<>(hours);
			}
			return new ArrayList<>(groupHourCandles(hours, type.getHourGroup()));
		}

		if (type == CandleType.DAY) {
			List<CandleDay> days = candleDayRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			days.sort(Comparator.comparing(CandleDay::getDate));
			return new ArrayList<>(days);
		}
		throw new IllegalArgumentException("지원하지 않는 타입: " + type);
	}

	/**
	 * 실시간 캐시 후속 병합 분기 전담 유틸
	 */
	private void appendLatestRealtimeCandle(CandleType type, List<CandleDTO> result, String stockCode) {
		if (type.isMinuteType()) {
			addCurrentCandleIfNewer(result, stockCode);
		} else if (type.isHourType()) {
			addCurrentCandleIfNewer(result, stockCode);
		} else if (type == CandleType.DAY) {
			addCandleIfNewer(result, getTodayCandle(stockCode));
		}
	}

	// ===================== 📊 분봉 그룹화 연산 =====================

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

	// ===================== 📊 시봉 그룹화 연산 (분봉과 동일 패턴) =====================

	private List<CandleHour> groupHourCandles(List<CandleHour> candles, int hourGroupSize) {
		return new ArrayList<>(
				candles.stream().collect(Collectors.groupingBy(c -> floorHour(c.getTime(), hourGroupSize))).entrySet()
						.stream().sorted(Map.Entry.comparingByKey())
						.map(entry -> toGroupedCandleHour(entry.getKey(), entry.getValue())).toList());
	}

	private CandleHour toGroupedCandleHour(LocalDateTime time, List<CandleHour> group) {
		List<CandleHour> sortedGroup = new ArrayList<>(group);
		sortedGroup.sort(Comparator.comparing(CandleHour::getTime));
		CandleHour first = sortedGroup.get(0);
		CandleHour last = sortedGroup.get(sortedGroup.size() - 1);
		int high = sortedGroup.stream().mapToInt(CandleHour::getHigh).max().orElse(first.getHigh());
		int low = sortedGroup.stream().mapToInt(CandleHour::getLow).min().orElse(first.getLow());
		long sellQty = sortedGroup.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long buyQty = sortedGroup.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
		long tradeAmount = sortedGroup.stream().mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0L)
				.sum();
		return new CandleHour(null, first.getStockCode(), time, first.getOpen(), high, low, last.getClose(), buyQty,
				sellQty, buyQty + sellQty, tradeAmount);
	}

	private LocalDateTime floorHour(LocalDateTime time, int hourGroupSize) {
		return time.withHour((time.getHour() / hourGroupSize) * hourGroupSize).withMinute(0).withSecond(0).withNano(0);
	}

	// ===================== 🧬 명시적 Candle 헬퍼 메서드군 =====================

	private List<CandleWithMA<Candle>> getCachedCandlesInRange(CandleType type, String stockCode, String fromStr,
			Predicate<Candle> inRange) {
		List<CandleWithMA<Candle>> cache = candleCacheService.getCandles(type, stockCode);
		if (!isCacheCoveringRangeStr(cache, fromStr)) {
			log.info("캐시 범위 미달 혹은 비어있음");
			return List.of();
		}
		return cache.stream().filter(c -> inRange.test(c.getCandle())).toList();
	}

	private boolean isCacheCoveringRangeStr(List<CandleWithMA<Candle>> cache, String fromStr) {
		if (cache == null || cache.isEmpty())
			return false;
		String cacheStart = cache.get(0).getCandle().getCandleTime();
		String cacheEnd = cache.get(cache.size() - 1).getCandle().getCandleTime();
		return fromStr.compareTo(cacheStart) >= 0 && fromStr.compareTo(cacheEnd) <= 0;
	}

	private boolean isBetweenStr(String time, String fromStr, String toStr) {
		return time.compareTo(fromStr) >= 0 && time.compareTo(toStr) <= 0;
	}

	private List<CandleWithMA<Candle>> wrapWithEmptyMA(List<Candle> candles) {
		return candles.stream().map(c -> new CandleWithMA<>(c, new HashMap<Integer, Double>()))
				.collect(Collectors.toList());
	}

	private List<CandleDTO> toDTOList(List<CandleWithMA<Candle>> wrappedCache) {
		return wrappedCache.stream().map(CandleDTO::from).collect(Collectors.toList());
	}

	private void calculateMovingAveragesInPlace(List<CandleWithMA<Candle>> list) {
		for (int i = 0; i < list.size(); i++) {
			Map<Integer, Double> maMap = list.get(i).getMa();
			for (int period : MA_PERIODS) {
				int windowStart = Math.max(0, i - period + 1);
				double sum = 0;
				for (int j = windowStart; j <= i; j++) {
					sum += list.get(j).getCandle().getClose();
				}
				double avg = sum / (i - windowStart + 1);
				maMap.put(period, Math.round(avg * 100.0) / 100.0);
			}
		}
	}

	// ===================== ⏱️ 공통 유틸 및 실시간 Redis 바인딩 =====================

	private void addCurrentCandleIfNewer(List<CandleDTO> result, String stockCode) {
		addCandleIfNewer(result, getCurrentCandleFromRedis(stockCode));
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
		return time.length() == 10 ? LocalDate.parse(time).atStartOfDay() : LocalDateTime.parse(time);
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