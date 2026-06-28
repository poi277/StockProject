package Poi.Stock.features.Candle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
public class CandleSchedulerService {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	private final RedisTemplate<String, String> redisTemplate;
	private final CandleMinuteRepository candleMinuteRepository;
	private final CandleHourRepository candleHourRepository;

	private final CandleWeekRepository candleWeekRepository;
	private final CandleMonthRepository candleMonthRepository;
	private final CandleYearRepository candleYearRepository;

	private final CandleDayRepository candleDayRepository;
	private final CandleCacheService candleCacheService;
	private final WebSocketService webSocketService;
	private final CandleCommonService candleCommonService;

	private static final String UPDATE_CANDLE_SCRIPT = """
			local candleKey   = KEYS[1]
			local price       = tonumber(ARGV[1])
			local buyQty      = tonumber(ARGV[2])
			local sellQty     = tonumber(ARGV[3])
			local tradeAmount = tonumber(ARGV[4])

			local exists = redis.call('EXISTS', candleKey)
			if exists == 0 then
			    redis.call('HSET', candleKey, 'open', price, 'high', price, 'low', price, 'close', price,
			        'buyQty', buyQty, 'sellQty', sellQty, 'tradeAmount', tradeAmount)
			    redis.call('EXPIRE', candleKey, 120)
			else
			    local high = tonumber(redis.call('HGET', candleKey, 'high'))
			    local low  = tonumber(redis.call('HGET', candleKey, 'low'))
			    if price > high then redis.call('HSET', candleKey, 'high', price) end
			    if price < low  then redis.call('HSET', candleKey, 'low',  price) end
			    redis.call('HSET',          candleKey, 'close',       price)
			    redis.call('HINCRBY',      candleKey, 'buyQty',      buyQty)
			    redis.call('HINCRBY',      candleKey, 'sellQty',     sellQty)
			    redis.call('HINCRBY',      candleKey, 'tradeAmount', tradeAmount)
			end

			return {
			    redis.call('HGET', candleKey, 'open'),
			    redis.call('HGET', candleKey, 'high'),
			    redis.call('HGET', candleKey, 'low'),
			    redis.call('HGET', candleKey, 'close')
			}
			""";

	/**
	 * 실시간 체결 엔진 연동 - 미확정 캔들 Redis 적재 및 DTO 반환
	 */
	public Map<CandleType, Candle> saveCurrentCandle(String stockCode, int price, int buyQty, int sellQty,
			long tradeAmount, LocalDateTime executionTime) {
		LocalDateTime minuteTime = executionTime.withSecond(0).withNano(0);
		String minuteCandleKey = "candle:1m:" + stockCode + ":" + minuteTime.format(FMT);

		String todayStr = executionTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String dayCandleKey = "candle:day:" + stockCode + ":" + todayStr;

		List<String> minuteResult = redisTemplate.execute(new DefaultRedisScript<>(UPDATE_CANDLE_SCRIPT, List.class),
				List.of(minuteCandleKey), String.valueOf(price), String.valueOf(buyQty), String.valueOf(sellQty),
				String.valueOf(tradeAmount));

		List<String> dayResult = redisTemplate.execute(new DefaultRedisScript<>(UPDATE_CANDLE_SCRIPT, List.class),
				List.of(dayCandleKey), String.valueOf(price), String.valueOf(buyQty), String.valueOf(sellQty),
				String.valueOf(tradeAmount));

		redisTemplate.expire(dayCandleKey, 24, TimeUnit.HOURS);

		if (minuteResult == null || minuteResult.size() < 4 || dayResult == null || dayResult.size() < 4) {
			return Map.of();
		}

		Candle minuteCandle = CandleDTO.current(minuteTime.toString(), Integer.parseInt(minuteResult.get(0)),
				Integer.parseInt(minuteResult.get(1)), Integer.parseInt(minuteResult.get(2)),
				Integer.parseInt(minuteResult.get(3)), (long) sellQty, (long) buyQty);

		Candle dayCandle = CandleDTO.current(executionTime.toLocalDate().toString(),
				Integer.parseInt(dayResult.get(0)),
				Integer.parseInt(dayResult.get(1)), Integer.parseInt(dayResult.get(2)),
				Integer.parseInt(dayResult.get(3)), (long) sellQty, (long) buyQty);

		return Map.of(CandleType.ONE_MINUTE, minuteCandle, CandleType.DAY, dayCandle);
	}

	public List<CandleMinute> save1MinCandle(List<String> assignedCodes, LocalDateTime now) {
		List<CandleMinute> savedCandles = new ArrayList<>();

		// 1. 분봉 정산 스케줄러 중복 방지를 위한 분산 락 획득
		Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock:candle", "1", 10, TimeUnit.SECONDS);
		if (!Boolean.TRUE.equals(lock)) {
			return savedCandles;
		}

		try {
			for (String stockCode : assignedCodes) {
				Set<String> keys = redisTemplate.keys("candle:1m:" + stockCode + ":*");
				if (keys == null || keys.isEmpty()) {
					continue;
				}
				for (String key : keys) {
					try {
						String timeStr = key.substring(key.lastIndexOf(":") + 1);
						LocalDateTime candleTime = LocalDateTime.parse(timeStr, FMT);
						if (!candleTime.isBefore(now.minusMinutes(1))) {
							continue;
						}
						Map<Object, Object> candleHash = redisTemplate.opsForHash().entries(key);
						if (candleHash == null || candleHash.isEmpty()) {
							continue;
						}
						CandleMinute candleMinute = Candle.fromRedisMap(candleHash,
								(open, high, low, close, bQty, sQty, vol, amt) -> new CandleMinute(null, stockCode,
										candleTime, open, high, low, close, bQty, sQty, vol, amt));
						candleMinuteRepository.save(candleMinute);
						savedCandles.add(candleMinute);
						redisTemplate.delete(key);

					} catch (Exception e) {
						log.error("1분봉 파일 유도 에러 - key: {} error: {}", key, e.getMessage(), e);
					}
				}
			}
		} finally {
			// 무조건 분산 락 해제
			redisTemplate.delete("lock:candle");
		}
		return savedCandles;
	}

	public void saveHourlyCandles(List<String> assignedCodes) {
		LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
		LocalDateTime startTime = now.minusHours(1);
		for (String stockCode : assignedCodes) {
			try {
				List<CandleMinute> minutes = candleMinuteRepository
						.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, startTime, now);
				if (minutes.isEmpty())
					continue;
				Candle candleHour = toGroupedCandle(minutes, startTime, CandleType.HOUR);
				candleHourRepository.save((CandleHour) candleHour);

				// Candle 상위 도메인 인터페이스 캐시 컴포넌트 호출 파이프라인 정형화
				CandleWithMA<Candle> wrapped = candleCacheService.upsertCandle(CandleType.HOUR, stockCode, candleHour);
				if (wrapped != null) {
					webSocketService.sendCompleteCandle(wrapped, stockCode, CandleType.HOUR);
				}
			} catch (Exception e) {
				log.error("60분봉 집계 에러 - 종목: {} error: {}", stockCode, e.getMessage());
			}
		}
	}

	public void saveDailyCandles(List<String> assignedCodes) {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		log.info("📢 장마감 전체 캔들(일/주/월/년) 통합 정산 시작 (빌더 패턴 적용): {}", todayStr);

        for (String stockCode : assignedCodes) {
            try {
                String dayKey = "candle:day:" + stockCode + ":" + todayStr;

                Map<Object, Object> redisDayFields = redisTemplate.opsForHash().entries(dayKey);

                if (redisDayFields == null || redisDayFields.isEmpty()) {
                    log.warn("종목 [{}] 의 금일 일봉 캐시가 Redis에 존재하지 않아 정산을 건너뜁니다.", stockCode);
                    continue;
                }
				CandleDay candleDay = Candle.fromRedisMap(redisDayFields,
						(open, high, low, close, bQty, sQty, vol, amt) -> new CandleDay(null, stockCode, today, open,
								high, low, close, bQty, sQty, vol, amt, 0, 0.0));
                // 전날 종가 기반 등락폭/등락률 계산
                int changeAmount = 0;
                double changeRate = 0.0;
                Optional<CandleDay> yesterdayCandle = candleDayRepository.findByStockCodeAndDate(stockCode, today.minusDays(1));
                
                if (yesterdayCandle.isPresent()) {
                    int yesterdayClose = yesterdayCandle.get().getClose();
                    changeAmount = candleDay.getClose() - yesterdayClose;
                    changeRate = yesterdayClose != 0 ? ((double) changeAmount / yesterdayClose) * 100.0 : 0.0;
                } else {
                    changeAmount = candleDay.getClose() - candleDay.getOpen();
                    changeRate = candleDay.getOpen() != 0 ? ((double) changeAmount / candleDay.getOpen()) * 100.0 : 0.0;
                }

                // 등락 계산 결과 셋팅
                candleDay.setChangeAmount(changeAmount);
                candleDay.setChangeRate(changeRate);

                // 일봉 DB 저장
                candleDayRepository.save(candleDay);

				// 3. 🎯 [빌더 버전] 주 / 월 / 년봉 상위 주기 캔들 통합 정산
                
                // 3-1. 주봉 (해당 주의 월요일 기준)
                LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                Optional<CandleWeek> existingWeek = candleWeekRepository.findByStockCodeAndDate(stockCode, monday);

				CandleWeek newWeek = CandleWeek.builder().stockCode(stockCode).date(monday)
						.open(candleDay.getOpen()).high(candleDay.getHigh()).low(candleDay.getLow())
						.close(candleDay.getClose()).buyQty(candleDay.getBuyQty()).sellQty(candleDay.getSellQty())
						.totalVolume(candleDay.getTotalVolume()).tradeAmount(candleDay.getTradeAmount())
						// 등락폭/등락률 정보가 생성자에 정수/실수로 묶여있더라도 빌더에서는 불필요 시 생략이 가능합니다.
						.build();
                        
                candleCommonService.upsertUpperPeriodCandle(CandleType.WEEK, stockCode, monday, existingWeek, newWeek);

                // 3-2. 월봉 (해당 월의 1일 기준)
                LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                Optional<CandleMonth> existingMonth = candleMonthRepository.findByStockCodeAndDate(stockCode, firstDayOfMonth);

				CandleMonth newMonth = CandleMonth.builder().stockCode(stockCode).date(firstDayOfMonth)
						.open(candleDay.getOpen()).high(candleDay.getHigh()).low(candleDay.getLow())
						.close(candleDay.getClose()).buyQty(candleDay.getBuyQty()).sellQty(candleDay.getSellQty())
						.totalVolume(candleDay.getTotalVolume()).tradeAmount(candleDay.getTradeAmount()).build();
                        
                candleCommonService.upsertUpperPeriodCandle(CandleType.MONTH, stockCode, firstDayOfMonth, existingMonth, newMonth);

                // 3-3. 년봉 (해당 년도의 1월 1일 기준)
                LocalDate firstDayOfYear = today.with(TemporalAdjusters.firstDayOfYear());
                Optional<CandleYear> existingYear = candleYearRepository.findByStockCodeAndDate(stockCode, firstDayOfYear);

				CandleYear newYear = CandleYear.builder().stockCode(stockCode).date(firstDayOfYear)
						.open(candleDay.getOpen()).high(candleDay.getHigh()).low(candleDay.getLow())
						.close(candleDay.getClose()).buyQty(candleDay.getBuyQty()).sellQty(candleDay.getSellQty())
						.totalVolume(candleDay.getTotalVolume()).tradeAmount(candleDay.getTradeAmount()).build();

                candleCommonService.upsertUpperPeriodCandle(CandleType.YEAR, stockCode, firstDayOfYear, existingYear, newYear);

                // 4. 다음 날 깨끗한 시가로 출발할 수 있도록 오늘 자 일봉 캐시 삭제
                redisTemplate.delete(dayKey);

                // 5. 메모리 내 로컬 캐시 컴포넌트 싱크업 및 실시간 웹소켓 발행
                CandleWithMA<Candle> wrapped = candleCacheService.upsertCandle(CandleType.DAY, stockCode, candleDay);
                if (wrapped != null) {
                    webSocketService.sendCompleteCandle(wrapped, stockCode, CandleType.DAY);
                }

                log.info("종목 [{}] 장마감 전체 통합 정산 완료 (Redis 이관 -> 주/월/년 반영 완료)", stockCode);
            } catch (Exception e) {
                log.error("💥 통합 마감 에러 - 종목: {} error: {}", stockCode, e.getMessage(), e);
            }
        }
    }

	/**
	 * 스케줄러 후처리 - 1분봉 기반 상위 멀티 분봉 캐시 동적 빌드 파이프라인
	 */
	public void updateMinuteCaches(List<CandleMinute> savedCandles) {
		if (savedCandles == null || savedCandles.isEmpty())
			return;

		// 1. 종목 코드로 그룹핑
		Map<String, List<CandleMinute>> candlesByStockCode = savedCandles.stream()
				.collect(Collectors.groupingBy(CandleMinute::getStockCode));

		candlesByStockCode.forEach((stockCode, newCandles) -> {
			// 안전하게 List<Candle> 형태로 전달하기 위해 변환
			List<Candle> genericNewCandles = new ArrayList<>(newCandles);

			// 1분봉 캐시 업서트
			candleCacheService.upsertCandles(CandleType.ONE_MINUTE, stockCode, genericNewCandles);

			// Candle 기반 인터페이스로 직접 조회
			List<CandleWithMA<Candle>> oneMinCache = candleCacheService.getCacheCandles(CandleType.ONE_MINUTE, stockCode);
			if (oneMinCache.isEmpty())
				return;

			// 1분봉의 최신 확정 데이터 추출 및 웹소켓 전송 (Raw Type 캐스팅 완전 제거)
			CandleWithMA<Candle> latestOneMin = oneMinCache.get(oneMinCache.size() - 1);
			webSocketService.sendCompleteCandle(latestOneMin, stockCode, CandleType.ONE_MINUTE);

			// 2. 상위 분봉 동적 순회 (3, 5, 10, 30, 60 등)
			for (CandleType candleType : CandleType.values()) {
				if (!candleType.isMinuteType() || candleType == CandleType.ONE_MINUTE)
					continue;

				int candleMinute = candleType.getMinute();

				// 캐시 전체를 해당 분봉 기준으로 한번만 그룹핑
				Map<LocalDateTime, List<CandleMinute>> grouped = oneMinCache.stream().map(CandleWithMA::getCandle)
						.map(c -> (CandleMinute) c) // 내재 집계 연산을 위해 구체 클래스로 바인딩
						.collect(Collectors.groupingBy(c -> floorTime(c.getTime(), candleMinute)));

				List<LocalDateTime> targetLocalDateTime = newCandles.stream()
						.map(c -> floorTime(c.getTime(), candleMinute)).distinct().toList();

				for (LocalDateTime localDateTime : targetLocalDateTime) {
					List<CandleMinute> livePieces = grouped.get(localDateTime);

					if (livePieces == null || livePieces.isEmpty()) {
						continue;
					}
					Candle combinedCandle = toGroupedCandle(livePieces, localDateTime, candleType);

					// 상위 분봉 캐시 업서트 및 웹소켓 발행
					CandleWithMA<Candle> wrapped = candleCacheService.upsertCandle(candleType, stockCode,
							combinedCandle);
					if (wrapped != null) {
						webSocketService.sendCompleteCandle(wrapped, stockCode, candleType);
					}
				}
			}
		});
	}

	public List<Candle> convertToMinute(List<CandleMinute> minutes, CandleType type) {
		if (minutes == null || minutes.isEmpty()) {
			return List.of();
		}
		Map<LocalDateTime, List<CandleMinute>> grouped = minutes.stream().collect(Collectors
				.groupingBy(c -> floorTime(c.getTime(), type.getMinute()), TreeMap::new, Collectors.toList()));
		return grouped.entrySet().stream()
				.map(entry -> toGroupedCandle(entry.getValue(), entry.getKey(), type)).toList();
	}

	private LocalDateTime floorTime(LocalDateTime time, int minute) {
		int totalMinutes = time.getHour() * 60 + time.getMinute();
		int flooredMinutes = (totalMinutes / minute) * minute;
		return time.withHour(flooredMinutes / 60).withMinute(flooredMinutes % 60).withSecond(0).withNano(0);
	}


	private Candle toGroupedCandle(List<CandleMinute> group, LocalDateTime candleTime, CandleType type) {
		if (group == null || group.isEmpty()) {
			return null;
		}
		List<CandleMinute> sortedGroup = new ArrayList<>(group);
		sortedGroup.sort(Comparator.comparing(CandleMinute::getTime));
		CandleMinute first = sortedGroup.get(0);
		CandleMinute last = sortedGroup.get(sortedGroup.size() - 1);

		int open = first.getOpen();
		int close = last.getClose();
		int high = sortedGroup.stream().mapToInt(CandleMinute::getHigh).max().orElse(open);
		int low = sortedGroup.stream().mapToInt(CandleMinute::getLow).min().orElse(open);
		long buyQty = sortedGroup.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
		long sellQty = sortedGroup.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long tradeAmount = sortedGroup.stream().mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0L)
				.sum();
		long volume = buyQty + sellQty;
		if (type.isMinuteType()) {
			return new CandleMinute(null, first.getStockCode(), candleTime, open, high, low, close, buyQty, sellQty,
					volume, tradeAmount);
		} else {
			return new CandleHour(null, first.getStockCode(), candleTime, open, high, low, close, buyQty, sellQty,
					volume, tradeAmount);
		}
	}
}