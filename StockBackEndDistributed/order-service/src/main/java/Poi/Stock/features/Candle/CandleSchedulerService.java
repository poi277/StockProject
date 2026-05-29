package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleHourRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleSchedulerService {

	private final RedisTemplate<String, String> redisTemplate;
	private final CandleMinuteRepository candleMinuteRepository;
	private final CandleHourRepository candleHourRepository; 
	private final CandleDayRepository candleDayRepository;     
	private final StockRepository stockRepository;
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

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
			    redis.call('HSET',         candleKey, 'close',       price)
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
		return new CandleDTO(minuteTime.toString(), Integer.parseInt(result.get(0)), Integer.parseInt(result.get(1)),
				Integer.parseInt(result.get(2)), Integer.parseInt(result.get(3)), (long) sellQty, (long) buyQty);
	}

	public void save1MinCandle() {
		Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock:candle", "1", 10, TimeUnit.SECONDS);
		if (Boolean.FALSE.equals(lock))
			return;

		try {
			Set<String> keys = redisTemplate.keys("candle:1m:*");
			if (keys == null || keys.isEmpty())
				return;

			LocalDateTime now = LocalDateTime.now();
			for (String key : keys) {
				try {
					String prefix = "candle:1m:";
					String stockCode = key.substring(prefix.length(), key.lastIndexOf(":"));
					String timeStr = key.substring(key.lastIndexOf(":") + 1);
					LocalDateTime candleTime = LocalDateTime.parse(timeStr, FMT);

					if (!candleTime.isBefore(now.minusMinutes(1)))
						continue;

					Map<Object, Object> candle = redisTemplate.opsForHash().entries(key);
					if (candle.isEmpty())
						continue;

					CandleMinute candleMinute = CandleMinute.setCandleRedis(stockCode, candleTime, candle);
					candleMinuteRepository.save(candleMinute);
					redisTemplate.delete(key);
					log.info("1분봉 저장 완료 - {} {}", stockCode, candleTime);
				} catch (Exception e) {
					log.error("캔들 처리 실패 - key: {} error: {}", key, e.getMessage());
				}
			}
		} finally {
			redisTemplate.delete("lock:candle");
		}
	}
	public void saveHourlyCandles() {
		LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
		LocalDateTime startTime = now.minusHours(1);

		List<String> stockCodes = candleMinuteRepository.findDistinctStockCodes();
		for (String stockCode : stockCodes) {
			try {
				List<CandleMinute> minutes = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(
						stockCode, startTime, now);

				if (minutes.isEmpty())
					continue;

				CandleMinute first = minutes.get(0);
				CandleMinute last = minutes.get(minutes.size() - 1);

				int open = first.getOpen();
				int close = last.getClose();
				int high = minutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(open);
				int low = minutes.stream().mapToInt(CandleMinute::getLow).min().orElse(open);

				long buyQty = minutes.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
				long sellQty = minutes.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
				long totalVolume = buyQty + sellQty;
				
				long tradeAmount = minutes.stream()
						.mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount().longValue() : 0L).sum();

				CandleHour candleHour = new CandleHour(
						null, stockCode, startTime, open, high, low, close, buyQty, sellQty, totalVolume, tradeAmount
				);

				candleHourRepository.save(candleHour);
				log.info("60분봉 집계 완료 - {} 시간대: {}", stockCode, startTime);
			} catch (Exception e) {
				log.error("60분봉 집계 에러 - 종목: {} error: {}", stockCode, e.getMessage());
			}
		}
	}

	/**
	 * 일봉 집계 및 마감 저장 (순수 long으로 CandleDay 매핑)
	 */
	public void saveDailyCandles() {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

		List<String> stockCodes = candleMinuteRepository.findDistinctStockCodes();
		for (String stockCode : stockCodes) {
			try {
				List<CandleMinute> dayMinutes = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(
						stockCode, startOfDay, endOfDay);

				if (dayMinutes.isEmpty())
					continue;

				CandleMinute first = dayMinutes.get(0);
				CandleMinute last = dayMinutes.get(dayMinutes.size() - 1);

				int open = first.getOpen();
				int close = last.getClose();
				int high = dayMinutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(open);
				int low = dayMinutes.stream().mapToInt(CandleMinute::getLow).min().orElse(open);

				long buyQty = dayMinutes.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
				long sellQty = dayMinutes.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
				long totalVolume = buyQty + sellQty;

				long tradeAmount = dayMinutes.stream()
						.mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount().longValue() : 0L).sum();

				int changeAmount = 0;
				double changeRate = 0.0;

				Optional<CandleDay> yesterdayCandle = candleDayRepository.findByStockCodeAndDate(stockCode, today.minusDays(1));
				if (yesterdayCandle.isPresent()) {
					int yesterdayClose = yesterdayCandle.get().getClose();
					changeAmount = close - yesterdayClose;
					changeRate = ((double) changeAmount / yesterdayClose) * 100.0;
				} else {
					changeAmount = close - open;
					changeRate = open != 0 ? ((double) changeAmount / open) * 100.0 : 0.0;
				}

				CandleDay candleDay = new CandleDay(null, stockCode, today, open, high, low, close, buyQty, sellQty,
						totalVolume, tradeAmount, changeAmount, changeRate
				);

				candleDayRepository.save(candleDay);
				log.info("일봉 마감 완료 - 종목: {}, 날짜: {}, 등락률: {}%", stockCode, today, String.format("%.2f", changeRate));
			} catch (Exception e) {
				log.error("일봉 마감 에러 - 종목: {} error: {}", stockCode, e.getMessage());
			}
		}
	}
}