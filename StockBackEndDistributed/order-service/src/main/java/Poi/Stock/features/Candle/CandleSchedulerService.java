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
	private static final String UPDATE_CANDLE_SCRIPT = "local key = KEYS[1] " + "local price = tonumber(ARGV[1]) "
			+ "local qty = tonumber(ARGV[2]) " + "local exists = redis.call('EXISTS', key) " + "if exists == 0 then "
			+ "  redis.call('HSET', key, 'open', price, 'high', price, 'low', price, 'close', price, 'volume', qty) "
			+ "  redis.call('EXPIRE', key, 120) " + // 2분 TTL
			"else " + "  local high = tonumber(redis.call('HGET', key, 'high')) "
			+ "  local low = tonumber(redis.call('HGET', key, 'low')) "
			+ "  local volume = tonumber(redis.call('HGET', ke,y, 'volume')) "
			+ "  if price > high then redis.call('HSET', key 'high', price) end "
			+ "  if price < low then redis.call('HSET', key, 'low', price) end "
			+ "  redis.call('HSET', key, 'close', price) " + "  redis.call('HSET', key, 'volume', volume + qty) "
			+ "end " + "return 1";

	// 체결 시 호출
	// 분리시 카프카로 호출
	public void saveCurrentCandle(String stockCode, int price, int quantity, LocalDateTime executionTime) {
		LocalDateTime minuteTime = executionTime.withSecond(0).withNano(0);
		String key = "candle:1m:" + stockCode + ":" + minuteTime.format(FMT);
		redisTemplate.execute(new DefaultRedisScript<>(UPDATE_CANDLE_SCRIPT, Long.class), List.of(key),
				String.valueOf(price), String.valueOf(quantity));
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
		List<String> stockCodes = candleMinuteRepository.findDistinctStockCode();
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
