package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.repository.CandleMinuteRepository;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleSchedulerService {

	private final RedisTemplate<String, String> redisTemplate;
	private final CandleMinuteRepository candleMinuteRepository;
	private final StockRepository stockRepository;
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	// Lua Script (원자성 + TTL)
	private static final String UPDATE_CANDLE_SCRIPT = """
			local candleKey = KEYS[1]
			local tradeKey  = KEYS[2]
			local price       = tonumber(ARGV[1])
			local qty         = tonumber(ARGV[2])
			local buyQty      = tonumber(ARGV[3])
			local sellQty     = tonumber(ARGV[4])
			local tradeAmount = tonumber(ARGV[5])

			-- 캔들 (기존 로직 그대로)
			local exists = redis.call('EXISTS', candleKey)
			if exists == 0 then
			    redis.call('HSET', candleKey, 'open', price, 'high', price, 'low', price, 'close', price, 'volume', qty)
			    redis.call('EXPIRE', candleKey, 120)
			else
			    local high = tonumber(redis.call('HGET', candleKey, 'high'))
			    local low  = tonumber(redis.call('HGET', candleKey, 'low'))
			    local volume = tonumber(redis.call('HGET', candleKey, 'volume'))
			    if price > high then redis.call('HSET', candleKey, 'high', price) end
			    if price < low  then redis.call('HSET', candleKey, 'low', price)  end
			    redis.call('HSET', candleKey, 'close', price)
			    redis.call('HSET', candleKey, 'volume', volume + qty)
			end

			-- 30분 통계 (추가)
			if redis.call('EXISTS', tradeKey) == 0 then
			    redis.call('EXPIRE', tradeKey, 1860)
			end
			redis.call('HINCRBY',      tradeKey, 'buyQty',      buyQty)
			redis.call('HINCRBY',      tradeKey, 'sellQty',     sellQty)
			redis.call('HINCRBYFLOAT', tradeKey, 'tradeAmount', tradeAmount)

			return {
			    redis.call('HGET', candleKey, 'open'),
			    redis.call('HGET', candleKey, 'high'),
			    redis.call('HGET', candleKey, 'low'),
			    redis.call('HGET', candleKey, 'close'),
			    redis.call('HGET', candleKey, 'volume')
			}
			""";

	// 체결 시 호출
	// 분리시 카프카로 호출
	public CandleDTO saveCurrentCandle(String stockCode, int price, int buyQty, int sellQty, long tradeAmount,
			LocalDateTime executionTime) {
		LocalDateTime minuteTime = executionTime.withSecond(0).withNano(0);
		LocalDateTime slot = executionTime.withMinute((executionTime.getMinute() / 30) * 30).withSecond(0).withNano(0);

		String candleKey = "candle:1m:" + stockCode + ":" + minuteTime.format(FMT);
		String tradeKey = "trade:30m:" + stockCode + ":" + slot.format(FMT);

		int totalQty = buyQty + sellQty;

		List<String> result = redisTemplate.execute(
				new DefaultRedisScript<>(UPDATE_CANDLE_SCRIPT, List.class), List.of(candleKey, tradeKey),
				String.valueOf(price), String.valueOf(totalQty), String.valueOf(buyQty), String.valueOf(sellQty),
				String.valueOf(tradeAmount)
		);

		if (result == null || result.size() < 5)
			return null;
		return new CandleDTO(minuteTime.toString(), Integer.parseInt(result.get(0)), Integer.parseInt(result.get(1)),
				Integer.parseInt(result.get(2)), Integer.parseInt(result.get(3)), Long.parseLong(result.get(4)));
	}

	public void save1MinCandle() {
		Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock:candle", "1", 10, TimeUnit.SECONDS);
		if (Boolean.FALSE.equals(lock)) {
			return;
		}
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
					if (!candleTime.isBefore(now.minusMinutes(1))) {
						continue;
					}
					Map<Object, Object> current = redisTemplate.opsForHash().entries(key);
					if (current.isEmpty())
						continue;
					CandleMinute candle = CandleMinute.setCandleRedis(stockCode, candleTime, current);
					candleMinuteRepository.save(candle);
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
	public void saveDailyStock() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		List<String> stockCodes = candleMinuteRepository.findDistinctStockCodes();
		for (String stockCode : stockCodes) {
			List<CandleMinute> minutes = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay());
			if (minutes.isEmpty())
				continue;
			CandleMinute first = minutes.get(0);
			CandleMinute last = minutes.get(minutes.size() - 1);
			int open = first.getOpen();
			int close = last.getClose();
			int high = minutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(0);
			int low = minutes.stream().mapToInt(CandleMinute::getLow).min().orElse(0);
			long volume = minutes.stream().mapToLong(CandleMinute::getVolume).sum();
			Stock stock = new Stock(stockCode, yesterday, null, // stockName 필요하면 채우기
					open, high, low, close, volume, null, // value (선택)
					close - open, (double) (close - open) / open * 100);
			stockRepository.save(stock);
		}

		log.info("일봉 저장 완료 (Stock 테이블) - {}", yesterday);
	}
}
