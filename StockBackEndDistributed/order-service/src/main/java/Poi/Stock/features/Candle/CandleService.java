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

	// ===================== 통합 메인 엔트리포인트 =====================

	/**
	 * 기간별 캔들 조회 (통합 파이프라인)
	 */
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

		List<CandleWithMA<Candle>> wrappedCache = candleCacheService.getCandles(type, stockCode);
		if (wrappedCache.isEmpty()) {
			log.info("{} 초기화 캐시 공백 - 종목: {}. DB에서 최신 데이터를 호출합니다.", type, stockCode);
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
		List<CandleDTO> result = toDTOList(wrappedCache);
		appendLatestRealtimeCandle(type, result, stockCode);
		mergeLiveCandle(type, result);
		return result;
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
		// 1. 분봉 계열 (Time 기준 정렬)
		case ONE_MINUTE, THREE_MINUTE, FIVE_MINUTE, TEN_MINUTE -> {
			List<CandleMinute> dbCandles = candleMinuteRepository.findTop100ByStockCodeOrderByTimeDesc(stockCode);
			dbCandles.sort(Comparator.comparing(CandleMinute::getTime));
			yield new ArrayList<>(dbCandles);
		}

		// 2. 시간봉 계열 (Time 기준 정렬)
		case HOUR, TWO_HOUR, THREE_HOUR, FOUR_HOUR -> {
			List<CandleHour> hours = candleHourRepository.findTop100ByStockCodeOrderByTimeDesc(stockCode);
			hours.sort(Comparator.comparing(CandleHour::getTime));
			yield new ArrayList<>(hours);
		}

		// 3. 일봉 (Date 기준 정렬)
		case DAY -> {
			List<CandleDay> days = candleDayRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			days.sort(Comparator.comparing(CandleDay::getDate));
			yield new ArrayList<>(days);
		}

		// 4. 주봉 (Date 기준 정렬)
		case WEEK -> {
			List<CandleWeek> weeks = candleWeekRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			weeks.sort(Comparator.comparing(CandleWeek::getDate));
			yield new ArrayList<>(weeks);
		}

		// 5. 월봉 (Date 기준 정렬)
		case MONTH -> {
			List<CandleMonth> months = candleMonthRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			months.sort(Comparator.comparing(CandleMonth::getDate));
			yield new ArrayList<>(months);
		}

		// 6. 년봉 (Date 기준 정렬)
		case YEAR -> {
			List<CandleYear> years = candleYearRepository.findTop100ByStockCodeOrderByDateDesc(stockCode);
			years.sort(Comparator.comparing(CandleYear::getDate));
			yield new ArrayList<>(years);
		}

		default -> throw new IllegalArgumentException("지원하지 않는 타입: " + type);
		};
	}

	/**
	 * 실시간 캐시 후속 병합 분기 전담 유틸
	 */
	private void appendLatestRealtimeCandle(CandleType type, List<CandleDTO> result, String stockCode) {
		LocalDateTime now = LocalDateTime.now();

		if (type.isMinuteType() || type.isHourType()) {
			String timeStr = now.withSecond(0).withNano(0).format(FMT);
			addCandleIfNewer(result, type, getCurrentCandleFromRedis("1m", stockCode, timeStr));
		} else if (type == CandleType.DAY) {
			String todayStr = now.format(DAY_FMT);
			addCandleIfNewer(result, type, getCurrentCandleFromRedis("day", stockCode, todayStr));
		}
	}

	// ===================== 캔들 다형성 통합 그룹화 연산 레이어 =====================

	/**
	 * 🎯 분봉/시봉 통합 그룹화 및 묶음 단위 검증 조건 파이프라인 (바깥 호출용)
	 */
	private List<Candle> groupCandles(List<Candle> rawCandles, CandleType type) {
		if (rawCandles == null || rawCandles.isEmpty()) {
			return List.of();
		}

		// 1분봉이나 1시간봉처럼 묶음 연산(그룹화)이 필요 없는 기본 규격은 굳이 연산하지 않고 무사통과(Pass)
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

	/**
	 * 하위 캔들 세트를 단일 Candle 인터페이스 구현체로 축약 집계
	 */
	private Candle toGroupedCandle(String timeStr, List<? extends Candle> group) {
		List<? extends Candle> sortedGroup = new ArrayList<>(group);
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

		return new CandleMinute(null, first.getStockCode(), time, open, high, low, close, buyQty,
				sellQty, buyQty + sellQty, tradeAmount);
	}

	/**
	 * CandleType 속성에 따라 시간 절삭 단위를 동적으로 계산
	 */
	private String floorTime(String candleTimeStr, CandleType type) {
		LocalDateTime time = candleTimeStr.length() == 12 ? LocalDateTime.parse(candleTimeStr, FMT)
				: LocalDateTime.parse(candleTimeStr);

		if (type.isMinuteType()) {
			int minute = type.getMinute();
			time = time.withMinute((time.getMinute() / minute) * minute).withSecond(0).withNano(0);
		} else if (type.isHourType()) {
			int hourGroup = type.getHourGroup();
			time = time.withHour((time.getHour() / hourGroup) * hourGroup).withMinute(0).withSecond(0).withNano(0);
		}

		return time.format(FMT);
	}

	// ===================== 명시적 Candle 헬퍼 메서드군 =====================

	private List<CandleWithMA<Candle>> getCachedCandlesInRange(CandleType type, String stockCode, String fromStr,
			String toStr, Predicate<Candle> inRange) {
		List<CandleWithMA<Candle>> cache = candleCacheService.getCandles(type, stockCode);

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

	// ===================== ⏱️ 공통 유틸 및 실시간 Redis 바인딩 =====================

	private void addCandleIfNewer(List<CandleDTO> result, CandleType type, CandleDTO candle) {
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

	private CandleDTO getCurrentCandleFromRedis(String candleTypePrefix, String stockCode, String timeSuffix) {
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

			if ("day".equals(candleTypePrefix)) {
				LocalDate dayDate = LocalDate.parse(timeSuffix, DAY_FMT);
				return CandleDTO.today(dayDate, Integer.parseInt(String.valueOf(open)),
						Integer.parseInt(String.valueOf(high)), Integer.parseInt(String.valueOf(low)),
						Integer.parseInt(String.valueOf(close)), parseLong(current.get("sellQty")),
						parseLong(current.get("buyQty")));
			} else {
				LocalDateTime minuteTime = LocalDateTime.parse(timeSuffix, FMT);
				return CandleDTO.current(minuteTime, Integer.parseInt(String.valueOf(open)),
						Integer.parseInt(String.valueOf(high)), Integer.parseInt(String.valueOf(low)),
						Integer.parseInt(String.valueOf(close)), parseLong(current.get("sellQty")),
						parseLong(current.get("buyQty")));
			}
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
		Map<CandleType, CandleDTO> candles = candleSchedulerService.saveCurrentCandle(stockCode, currentPrice, buyQty,
				sellQty, tradeAmount, lastExecutionTime);

		candles.forEach((type, candleDTO) -> webSocketService.sendCurrentCandle(candleDTO, stockCode, type));
	}

	private void mergeLiveCandle(CandleType type, List<CandleDTO> result) {
		// 데이터가 없거나, 실시간 데이터 1개만 덜렁 있는 경우는 비교 대상이 없으므로 통과
		// 일은 나중에 추가할테니 우선은 통과
		if (result == null || result.size() < 2 || type == CandleType.DAY) {
			return;
		}
		// 1. 맨 마지막에 붙은 실시간 캔들(Live)과 그 직전 확정 캔들(Last)을 꺼냅니다.
		CandleDTO liveCandle = result.get(result.size() - 1);
		CandleDTO lastCandle = result.get(result.size() - 2);

		// 2. 두 캔들의 시간 포맷 정형화 (yyyyMMddHHmm 12자리 추출)
		String liveTimeStr = liveCandle.getTime().replace("-", "").replace("T", "").replace(":", "").substring(0, 12);
		String lastTimeStr = lastCandle.getTime().replace("-", "").replace("T", "").replace(":", "").substring(0, 12);

		// 3. 실시간 1분봉 조각의 시간을 현재 차트 주기(예: 3분, 5분)에 맞게 올림/절삭(floorTime) 연산합니다.
		String targetTimeStr = floorTime(liveTimeStr, type);

		// 4. 시간 비교 세그먼트
		if (targetTimeStr.equals(lastTimeStr)) {
			// 🤝 Case A: 실시간 조각의 주기 시간이 직전 캔들 시간과 같다! (기존 캔들에 합산 후 실시간 단독 봉 제거)
			lastCandle.setHigh(Math.max(lastCandle.getHigh(), liveCandle.getHigh()));
			lastCandle.setLow(Math.min(lastCandle.getLow(), liveCandle.getLow()));
			lastCandle.setClose(liveCandle.getClose()); // 종가는 실시간 최신 가격으로 동적 동기화
			lastCandle.setTotalVolume(lastCandle.getTotalVolume() + liveCandle.getTotalVolume());

			// 합산이 끝났으므로 맨 뒤에 붙었던 실시간 1분짜리 임시 봉은 제거합니다.
			result.remove(result.size() - 1);
		} else {
			// ➕ Case B: 실시간 조각의 주기가 직전 캔들 시간보다 미래다! (새로운 n분봉 주기의 시작)
			// 실시간 봉의 타임스탬프만 n분봉 기준 정렬된 시간(targetTimeStr)으로 교체하여 독립된 진행 중인 봉으로 유지합니다.
			LocalDateTime targetLdt = LocalDateTime.parse(targetTimeStr, FMT);
			liveCandle.setTime(targetLdt.toString());
		}
	}
}