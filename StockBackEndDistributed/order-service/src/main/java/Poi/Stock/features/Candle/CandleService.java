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
import Poi.Stock.features.Candle.Entity.CandleMonth;
import Poi.Stock.features.Candle.Entity.CandleWeek;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.features.Candle.Entity.CandleYear;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleHourRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.Candle.repository.CandleMonthRepository;
import Poi.Stock.features.Candle.repository.CandleWeekRepository;
import Poi.Stock.features.Candle.repository.CandleYearRepository;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final int[] MA_PERIODS = { 5, 20, 60 };

	private final CandleMinuteRepository candleMinuteRepository;
	private final CandleDayRepository candleDayRepository;

	private final CandleHourRepository candleHourRepository;
	private final CandleWeekRepository candleWeekRepository;
	private final CandleMonthRepository candleMonthRepository;
	private final CandleYearRepository candleYearRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final CandleSchedulerService candleSchedulerService;
	private final CandleCacheService candleCacheService;
	private final WebSocketService webSocketService;

	public List<CandleDTO> getCandle(CandleType type, String stockCode, String startTime, String endTime) {
		LocalDateTime from = parseStartTime(startTime);
		LocalDateTime to = parseEndTime(endTime);
		String fromStr = from.toString();
		String toStr = to.toString();

		List<CandleWithMA<Candle>> wrappedCache = getCachedCandlesInRange(type, stockCode, fromStr, toStr,
				c -> isBetweenStr(c.getCandleTime(), fromStr, toStr));

		if (wrappedCache.isEmpty()) {
			List<Candle> dbCandles = loadCandlesFromDb(type, stockCode, from, to);
			List<Candle> processedCandles = groupCandles(dbCandles, type);
			List<CandleWithMA<Candle>> wrapped = wrapWithEmptyMA(processedCandles);
			calculateMovingAveragesInPlace(wrapped);
			return toDTOList(wrapped);
		}
		return toDTOList(wrappedCache);
	}

	public List<CandleDTO> getCandleInit(CandleType type, String stockCode) {
		int targetSize = 100;
		List<CandleWithMA<Candle>> wrappedCache = candleCacheService.getCacheCandles(type, stockCode);
		if (wrappedCache.isEmpty()) {
			List<Candle> dbCandles = loadTop100FromDb(type, stockCode);
			List<Candle> processedCandles = groupCandles(dbCandles, type);
			wrappedCache = wrapWithEmptyMA(processedCandles);
			calculateMovingAveragesInPlace(wrappedCache);
		} else {
			int cacheSize = wrappedCache.size();
			wrappedCache = wrappedCache.subList(Math.max(0, cacheSize - targetSize), cacheSize);
		}
		if (wrappedCache.isEmpty())
			return List.of();
		appendLatestRealtimeCandle(type, wrappedCache, stockCode);
		mergeLiveCandle(type, wrappedCache);
		calculateMovingAveragesLiveCandle(wrappedCache);
		List<CandleDTO> result = toDTOList(wrappedCache);
		return result;
	}


	private void calculateMovingAveragesLiveCandle(List<CandleWithMA<Candle>> wrappedCache) {
		if (wrappedCache == null || wrappedCache.isEmpty()) {
			return;
		}
		int i = wrappedCache.size() - 1;
		CandleWithMA<Candle> lastWrapped = wrappedCache.get(i);
		Map<Integer, Double> maMap = lastWrapped.getMa();
		if (maMap == null) {
			maMap = new HashMap<>();
		}
		for (int period : MA_PERIODS) {
			int windowStart = Math.max(0, i - period + 1);
			double sum = 0;
			for (int j = windowStart; j <= i; j++) {
				sum += wrappedCache.get(j).getCandle().getClose();
			}
			double avg = sum / (i - windowStart + 1);
			double roundAvg = Math.round(avg * 100.0) / 100.0;
			try {
				maMap.put(period, roundAvg);
			} catch (UnsupportedOperationException e) {
				maMap = new HashMap<>(maMap);
				maMap.put(period, roundAvg);
				lastWrapped.setMa(maMap);
			}
		}
	}

	private List<Candle> loadCandlesFromDb(CandleType type, String stockCode, LocalDateTime from, LocalDateTime to) {
		return switch (type) {
		case ONE_MINUTE, THREE_MINUTE, FIVE_MINUTE, TEN_MINUTE ->
			new ArrayList<>(candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, from, to));
		case HOUR, TWO_HOUR, THREE_HOUR, FOUR_HOUR ->
			new ArrayList<>(candleHourRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, from, to));
		case DAY -> new ArrayList<>(candleDayRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
				from.toLocalDate(), to.toLocalDate()));
		case WEEK -> new ArrayList<>(candleWeekRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
				from.toLocalDate(), to.toLocalDate()));
		case MONTH -> new ArrayList<>(candleMonthRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
				from.toLocalDate(), to.toLocalDate()));
		case YEAR -> new ArrayList<>(candleYearRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
				from.toLocalDate(), to.toLocalDate()));
		default -> throw new IllegalArgumentException("지원하지 않는 타입: " + type);
		};
	}

	/**
	 * DB에서 최신 Top 100 원천 데이터(Raw Data)만 조회하는 전담 메서드
	 */
	private List<Candle> loadTop100FromDb(CandleType type, String stockCode) {
		return switch (type) {
		case ONE_MINUTE, THREE_MINUTE, FIVE_MINUTE, TEN_MINUTE -> {
			List<CandleMinute> dbCandles = candleMinuteRepository.findTop100ByStockCodeOrderByTimeDesc(stockCode);
			dbCandles.sort(Comparator.comparing(CandleMinute::getTime));
			yield new ArrayList<>(dbCandles);
		}
		case HOUR, TWO_HOUR, THREE_HOUR, FOUR_HOUR -> {
			List<CandleHour> hours = candleHourRepository.findTop100ByStockCodeOrderByTimeDesc(stockCode);
			hours.sort(Comparator.comparing(CandleHour::getTime));
			yield new ArrayList<>(hours);
		}
		case DAY -> {
			List<CandleDay> days = candleDayRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			days.sort(Comparator.comparing(CandleDay::getDate));
			yield new ArrayList<>(days);
		}
		case WEEK -> {
			List<CandleWeek> weeks = candleWeekRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			weeks.sort(Comparator.comparing(CandleWeek::getDate));
			yield new ArrayList<>(weeks);
		}
		case MONTH -> {
			List<CandleMonth> months = candleMonthRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			months.sort(Comparator.comparing(CandleMonth::getDate));
			yield new ArrayList<>(months);
		}
		case YEAR -> {
			List<CandleYear> years = candleYearRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			years.sort(Comparator.comparing(CandleYear::getDate));
			yield new ArrayList<>(years);
		}

		default -> throw new IllegalArgumentException("지원하지 않는 타입: " + type);
		};
	}

	private void appendLatestRealtimeCandle(CandleType type, List<CandleWithMA<Candle>> wrappedCache,
			String stockCode) {
		LocalDateTime now = LocalDateTime.now();

		if (type.isMinuteType() || type.isHourType()) {
			String timeStr = now.withSecond(0).withNano(0).format(FMT);
			addCandleIfNewer(wrappedCache, type, getCurrentCandleFromRedis("1m", stockCode, timeStr));
		} else if (type == CandleType.DAY || type == CandleType.WEEK || type == CandleType.MONTH
				|| type == CandleType.YEAR) {
			String todayStr = now.format(DAY_FMT);
			addCandleIfNewer(wrappedCache, type, getCurrentCandleFromRedis("day", stockCode, todayStr));
		}
	}

	private List<Candle> groupCandles(List<Candle> rawCandles, CandleType type) {
		if (rawCandles == null || rawCandles.isEmpty()) {
			return List.of();
		}
		if ((type.isMinuteType() && type.getMinute() == 1) || (type.isHourType() && type.getHourGroup() == 1)
				|| type == CandleType.DAY || type == CandleType.WEEK || type == CandleType.MONTH
				|| type == CandleType.YEAR) {
			return rawCandles;
		}
		return new ArrayList<>(rawCandles.stream()
				.<Candle>map(c -> c)
				.collect(Collectors.groupingBy(c -> floorTime(c.getCandleTime(), type), Collectors.toList())).entrySet()
				.stream().sorted(Map.Entry.comparingByKey())
				.map(entry -> toGroupedCandle(entry.getKey(), entry.getValue())).toList());
	}

	private Candle toGroupedCandle(String timeStr, List<Candle> group) {
		List<Candle> sortedGroup = new ArrayList<>(group);
		sortedGroup.sort(Comparator.comparing(Candle::getCandleTime));

		Candle first = sortedGroup.get(0);
		Candle last = sortedGroup.get(sortedGroup.size() - 1);

		int open = first.getOpen();
		int close = last.getClose();
		int high = sortedGroup.stream().mapToInt(Candle::getHigh).max().orElse(first.getHigh());
		int low = sortedGroup.stream().mapToInt(Candle::getLow).min().orElse(first.getLow());
		long sellQty = sortedGroup.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long buyQty = sortedGroup.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
		long tradeAmount = sortedGroup.stream().mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0L)
				.sum();

		LocalDateTime time = LocalDateTime.parse(timeStr, FMT);

		return CandleDTO.of(time.toString(), open, high, low, close, buyQty, sellQty, buyQty + sellQty, tradeAmount,
				Map.of());
	}

	private String floorTime(String candleTimeStr, CandleType type) {
	    LocalDateTime time = candleTimeStr.length() == 12 ? LocalDateTime.parse(candleTimeStr, FMT)
	            : LocalDateTime.parse(candleTimeStr);

	    if (type.isMinuteType()) {
	        int minute = type.getMinute();
	        time = time.withMinute((time.getMinute() / minute) * minute).withSecond(0).withNano(0);
	    } else if (type.isHourType()) {
	        int hourGroup = type.getHourGroup();
	        time = time.withHour((time.getHour() / hourGroup) * hourGroup).withMinute(0).withSecond(0).withNano(0);
	    } else if (type == CandleType.WEEK) {
			int dayOfWeek = time.getDayOfWeek().getValue();
	        time = time.minusDays(dayOfWeek - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
	    } else if (type == CandleType.MONTH) {
	        time = time.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
	    } else if (type == CandleType.YEAR) {
	        time = time.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
	    }

	    return time.format(FMT);
	}

	private List<CandleWithMA<Candle>> getCachedCandlesInRange(CandleType type, String stockCode, String fromStr,
			String toStr, Predicate<Candle> inRange) {
		List<CandleWithMA<Candle>> cache = candleCacheService.getCacheCandles(type, stockCode);
		if (!isCacheCoveringRangeStr(cache, fromStr, toStr)) {
			return List.of();
		}
		return cache.stream().filter(c -> inRange.test(c.getCandle())).toList();
	}

	private boolean isCacheCoveringRangeStr(List<CandleWithMA<Candle>> cache, String fromStr, String toStr) {
		if (cache == null || cache.isEmpty()) {
			return false;
		}

		String cacheStart = cache.get(0).getCandle().getCandleTime();
		String cacheEnd = cache.get(cache.size() - 1).getCandle().getCandleTime();

		return fromStr.compareTo(cacheStart) >= 0 && toStr.compareTo(cacheEnd) <= 0;
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

	private void addCandleIfNewer(List<CandleWithMA<Candle>> wrappedCache, CandleType type,
			CandleWithMA<Candle> candle) {
		if (candle == null)
			return;

		if (wrappedCache.isEmpty()) {
			wrappedCache.add(candle);
			return;
		}
		LocalDateTime lastTime = parseCandleTime(wrappedCache.get(wrappedCache.size() - 1).getCandle().getCandleTime());
		LocalDateTime newTime = parseCandleTime(candle.getCandle().getCandleTime());
		if (newTime.isAfter(lastTime)) {
			wrappedCache.add(candle);
		}
	}

	private LocalDateTime parseCandleTime(String time) {
		return time.length() == 10 ? LocalDate.parse(time).atStartOfDay() : LocalDateTime.parse(time);
	}

	private CandleWithMA<Candle> getCurrentCandleFromRedis(String candleTypePrefix, String stockCode,
			String timeSuffix) {
		String key = "candle:" + candleTypePrefix + ":" + stockCode + ":" + timeSuffix;
		Map<Object, Object> current = redisTemplate.opsForHash().entries(key);

		if (current == null || current.isEmpty())
			return null;

		try {
			Object open = current.get("open");
			Object high = current.get("high");
			Object low = current.get("low");
			Object close = current.get("close");

			if (open == null || high == null || low == null || close == null)
				return null;

			Candle candleCore;
			long sellQty = parseLong(current.get("sellQty"));
			long buyQty = parseLong(current.get("buyQty"));

			if ("day".equals(candleTypePrefix)) {
				LocalDate dayDate = LocalDate.parse(timeSuffix, DAY_FMT);
				candleCore = CandleDTO.current(dayDate.toString(), Integer.parseInt(String.valueOf(open)),
						Integer.parseInt(String.valueOf(high)), Integer.parseInt(String.valueOf(low)),
						Integer.parseInt(String.valueOf(close)), buyQty, sellQty);
			} else {
				LocalDateTime minuteTime = LocalDateTime.parse(timeSuffix, FMT);
				candleCore = CandleDTO.current(minuteTime.toString(), Integer.parseInt(String.valueOf(open)),
						Integer.parseInt(String.valueOf(high)), Integer.parseInt(String.valueOf(low)),
						Integer.parseInt(String.valueOf(close)), buyQty, sellQty);
			}
			return new CandleWithMA<>(candleCore, Map.of());

		} catch (Exception e) {
			log.error("Redis 현재 캔들 파싱 실패 - Prefix: {}, stockCode: {}, error: {}", candleTypePrefix, stockCode,
					e.getMessage());
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
		Map<CandleType, Candle> candles = candleSchedulerService.saveCurrentCandle(stockCode, currentPrice, buyQty,
				sellQty, tradeAmount, lastExecutionTime);

		candles.forEach((type, candle) -> webSocketService.sendCurrentCandle(candle, stockCode, type));
	}

	private void mergeLiveCandle(CandleType type, List<CandleWithMA<Candle>> wrappedCache) {
		if (wrappedCache == null || wrappedCache.size() < 2 || type == CandleType.DAY) {
			return;
		}
		CandleWithMA<Candle> liveWrapped = wrappedCache.get(wrappedCache.size() - 1);
		CandleWithMA<Candle> lastWrapped = wrappedCache.get(wrappedCache.size() - 2);
		Candle liveCandle = liveWrapped.getCandle();
		Candle lastCandle = lastWrapped.getCandle();
		if (type == CandleType.WEEK || type == CandleType.MONTH || type == CandleType.YEAR) {
			lastCandle.setHigh(Math.max(lastCandle.getHigh(), liveCandle.getHigh()));
			lastCandle.setLow(Math.min(lastCandle.getLow(), liveCandle.getLow()));
			lastCandle.setClose(liveCandle.getClose());
			lastCandle.setTotalVolume(lastCandle.getTotalVolume() + liveCandle.getTotalVolume());
			lastCandle.setTradeAmount(lastCandle.getTradeAmount() + liveCandle.getTradeAmount());
			wrappedCache.remove(wrappedCache.size() - 1);
			return;
		}
		String liveTimeStr = liveCandle.getCandleTime().replace("-", "").replace("T", "").replace(":", "").substring(0,
				12);
		String lastTimeStr = lastCandle.getCandleTime().replace("-", "").replace("T", "").replace(":", "").substring(0,
				12);
		String targetTimeStr = floorTime(liveTimeStr, type);
		if (targetTimeStr.equals(lastTimeStr)) {
			lastCandle.setHigh(Math.max(lastCandle.getHigh(), liveCandle.getHigh()));
			lastCandle.setLow(Math.min(lastCandle.getLow(), liveCandle.getLow()));
			lastCandle.setClose(liveCandle.getClose());
			lastCandle.setTotalVolume(lastCandle.getTotalVolume() + liveCandle.getTotalVolume());
			lastCandle.setTradeAmount(lastCandle.getTradeAmount() + liveCandle.getTradeAmount());
			wrappedCache.remove(wrappedCache.size() - 1);
		} else {
			LocalDateTime targetLdt = LocalDateTime.parse(targetTimeStr, FMT);
			liveCandle.setCandleTime(targetLdt.toString());
			wrappedCache.set(wrappedCache.size() - 1, new CandleWithMA<>(liveCandle, Map.of()));
		}
	}
}