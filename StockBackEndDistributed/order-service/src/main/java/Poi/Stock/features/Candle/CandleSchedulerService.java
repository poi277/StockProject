package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class CandleSchedulerService {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	private final RedisTemplate<String, String> redisTemplate;
	private final CandleMinuteRepository candleMinuteRepository;
	private final CandleHourRepository candleHourRepository;
	private final CandleDayRepository candleDayRepository;
	private final CandleCacheService candleCacheService;
	private final WebSocketService webSocketService;

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

	public CandleDTO saveCurrentCandle(String stockCode, int price, int buyQty, int sellQty, long tradeAmount,
			LocalDateTime executionTime) {
		LocalDateTime minuteTime = executionTime.withSecond(0).withNano(0);
		String candleKey = "candle:1m:" + stockCode + ":" + minuteTime.format(FMT);

		List<String> result = redisTemplate.execute(new DefaultRedisScript<>(UPDATE_CANDLE_SCRIPT, List.class),
				List.of(candleKey), String.valueOf(price), String.valueOf(buyQty), String.valueOf(sellQty),
				String.valueOf(tradeAmount));

		if (result == null || result.size() < 4)
			return null;

		return CandleDTO.current(minuteTime, Integer.parseInt(result.get(0)), Integer.parseInt(result.get(1)),
				Integer.parseInt(result.get(2)), Integer.parseInt(result.get(3)), (long) sellQty, (long) buyQty);
	}

	public List<CandleMinute> save1MinCandle(List<String> assignedCodes, LocalDateTime now) {
		List<CandleMinute> savedCandles = new ArrayList<>();
		Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock:candle", "1", 10, TimeUnit.SECONDS);
		if (!Boolean.TRUE.equals(lock))
			return savedCandles;

		try {
			for (String stockCode : assignedCodes) {
				Set<String> keys = redisTemplate.keys("candle:1m:" + stockCode + ":*");
				if (keys == null || keys.isEmpty())
					continue;
				for (String key : keys) {
					try {
						String timeStr = key.substring(key.lastIndexOf(":") + 1);
						LocalDateTime candleTime = LocalDateTime.parse(timeStr, FMT);
						if (!candleTime.isBefore(now.minusMinutes(1)))
							continue;

						Map<Object, Object> candle = redisTemplate.opsForHash().entries(key);
						if (candle.isEmpty())
							continue;

						CandleMinute candleMinute = CandleMinute.setCandleRedis(stockCode, candleTime, candle);
						candleMinuteRepository.save(candleMinute);
						savedCandles.add(candleMinute);
						redisTemplate.delete(key);
					} catch (Exception e) {
						log.error("1분봉 파일 유도 에러 - key: {} error: {}", key, e.getMessage());
					}
				}
			}
		} finally {
			redisTemplate.delete("lock:candle");
		}
		return savedCandles;
	}

	public void updateMinuteCaches(List<CandleMinute> savedCandles) {
		if (savedCandles == null || savedCandles.isEmpty())
			return;

		Map<String, List<CandleMinute>> candlesByStockCode = savedCandles.stream()
				.collect(Collectors.groupingBy(CandleMinute::getStockCode));

		candlesByStockCode.forEach((stockCode, newCandles) -> {
			candleCacheService.upsertCandles(CandleType.ONE_MINUTE, stockCode, newCandles);
			List<CandleWithMA<CandleMinute>> oneMinCache = candleCacheService.getCandles(CandleType.ONE_MINUTE,
					stockCode);
			if (oneMinCache.isEmpty())
				return;
			CandleWithMA<CandleMinute> latestOneMin = oneMinCache.get(oneMinCache.size() - 1);
			webSocketService.sendCompleteCandle((CandleWithMA) latestOneMin, stockCode, CandleType.ONE_MINUTE);

			// 2. 상위 분봉 동적 순회 (3, 5, 10, 30, 60 등)
			for (CandleType candleType : CandleType.values()) {
				if (!candleType.isMinuteType() || candleType == CandleType.ONE_MINUTE)
					continue;

				int candleMinute = candleType.getMinute();

				// 캐시 전체를 해당 분봉 기준으로 한번만 그룹핑
				Map<LocalDateTime, List<CandleMinute>> grouped = oneMinCache.stream().map(CandleWithMA::getCandle)
						.collect(Collectors.groupingBy(c -> floorTime(c.getTime(), candleMinute)));

				List<LocalDateTime> targetLocalDateTime = newCandles.stream()
						.map(c -> floorTime(c.getTime(), candleMinute)).distinct().toList();

				for (LocalDateTime localDateTime : targetLocalDateTime) {
					List<CandleMinute> livePieces = grouped.get(localDateTime);

					if (livePieces == null || livePieces.isEmpty()) {
						continue;
					}
					CandleMinute combinedCandle = toGroupedCandleMinute(livePieces, localDateTime);
					CandleWithMA<Candle> wrapped = candleCacheService.upsertCandle(candleType, stockCode,
							combinedCandle);
					if (wrapped != null) {
						webSocketService.sendCompleteCandle(wrapped, stockCode, candleType);
					}
				}
			}
		});
	}

	public List<CandleMinute> convertToMinute(List<CandleMinute> minutes, int minute) {
		if (minutes == null || minutes.isEmpty()) {
			return List.of();
		}

		Map<LocalDateTime, List<CandleMinute>> grouped = minutes.stream()
				.collect(Collectors.groupingBy(c -> floorTime(c.getTime(), minute), TreeMap::new, Collectors.toList()));

		return grouped.entrySet().stream()
				.map(entry -> toGroupedCandleMinute(entry.getValue(), entry.getKey()
		)).toList();
	}

	private LocalDateTime floorTime(LocalDateTime time, int minute) {
		int totalMinutes = time.getHour() * 60 + time.getMinute();

		int flooredMinutes = (totalMinutes / minute) * minute;

		return time.withHour(flooredMinutes / 60).withMinute(flooredMinutes % 60).withSecond(0).withNano(0);
	}

	private CandleMinute toGroupedCandleMinute(List<CandleMinute> group, LocalDateTime candleTime) {

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

		return new CandleMinute(null, first.getStockCode(), candleTime,
				open, high, low, close, buyQty, sellQty, buyQty + sellQty, tradeAmount);
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
				CandleHour candleHour = toCandleHour(stockCode, startTime, minutes);
				candleHourRepository.save(candleHour);
				CandleWithMA<Candle> wrapped = candleCacheService.upsertCandle(CandleType.HOUR, stockCode, candleHour);
				if (wrapped != null) {
					webSocketService.sendCompleteCandle(wrapped, stockCode, CandleType.HOUR);
				}
			} catch (Exception e) {
				log.error("60분봉 집계 에러 - 종목: {} error: {}", stockCode, e.getMessage());
			}
		}
	}

	private CandleHour toCandleHour(String stockCode, LocalDateTime time, List<CandleMinute> minutes) {
		CandleMinute first = minutes.get(0);
		CandleMinute last = minutes.get(minutes.size() - 1);
		int open = first.getOpen();
		int close = last.getClose();
		int high = minutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(open);
		int low = minutes.stream().mapToInt(CandleMinute::getLow).min().orElse(open);
		long buyQty = minutes.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
		long sellQty = minutes.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long tradeAmount = minutes.stream()
				.mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount().longValue() : 0L).sum();
		return new CandleHour(null, stockCode, time, open, high, low, close, buyQty, sellQty, buyQty + sellQty,
				tradeAmount);
	}

	public void saveDailyCandles(List<String> assignedCodes) {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
		for (String stockCode : assignedCodes) {
			try {
				List<CandleMinute> dayMinutes = candleMinuteRepository
						.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, startOfDay, endOfDay);
				if (dayMinutes.isEmpty())
					continue;
				CandleDay candleDay = toCandleDay(stockCode, today, dayMinutes);
				candleDayRepository.save(candleDay);
				CandleWithMA<Candle> wrapped = candleCacheService.upsertCandle(CandleType.DAY, stockCode, candleDay);
				if (wrapped != null) {
					webSocketService.sendCompleteCandle(wrapped, stockCode, CandleType.DAY);

				}
			} catch (Exception e) {
				log.error("일봉 마감 에러 - 종목: {} error: {}", stockCode, e.getMessage());
			}
		}
	}

	private CandleDay toCandleDay(String stockCode, LocalDate date, List<CandleMinute> minutes) {
		CandleMinute first = minutes.get(0);
		CandleMinute last = minutes.get(minutes.size() - 1);
		int open = first.getOpen();
		int close = last.getClose();
		int high = minutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(open);
		int low = minutes.stream().mapToInt(CandleMinute::getLow).min().orElse(open);
		long buyQty = minutes.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
		long sellQty = minutes.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
		long tradeAmount = minutes.stream()
				.mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount().longValue() : 0L).sum();
		int changeAmount;
		double changeRate;
		Optional<CandleDay> yesterdayCandle = candleDayRepository.findByStockCodeAndDate(stockCode, date.minusDays(1));
		if (yesterdayCandle.isPresent()) {
			int yesterdayClose = yesterdayCandle.get().getClose();
			changeAmount = close - yesterdayClose;
			changeRate = yesterdayClose != 0 ? ((double) changeAmount / yesterdayClose) * 100.0 : 0.0;
		} else {
			changeAmount = close - open;
			changeRate = open != 0 ? ((double) changeAmount / open) * 100.0 : 0.0;
		}
		return new CandleDay(null, stockCode, date, open, high, low, close, buyQty, sellQty, buyQty + sellQty,
				tradeAmount, changeAmount, changeRate);
	}

}