package Poi.Stock.Scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import Poi.Stock.features.Stock.StockTradeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTradeStatsScheduler {

	private final RedisTemplate<String, String> redisTemplate;
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	// stockCode → TradeStats
	private final Map<String, StockTradeStatus> stockTradeStatusCache = new ConcurrentHashMap<>();

	@Scheduled(fixedDelay = 60_000)
	public void refreshFromRedis() {
		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime slot = now.withMinute((now.getMinute() / 30) * 30).withSecond(0).withNano(0);
			String pattern = "trade:30m:*:" + slot.format(FMT);

			Set<String> keys = redisTemplate.keys(pattern);
			if (keys == null || keys.isEmpty())
				return;

			for (String key : keys) {
				try {
					// key 형식: trade:30m:{stockCode}:{slot}
					String[] parts = key.split(":");
					String stockCode = parts[2];

					Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
					if (data.isEmpty())
						continue;

					long buyQty = parseLong(data.get("buyQty"));
					long sellQty = parseLong(data.get("sellQty"));
					double amount = parseDouble(data.get("tradeAmount"));

					stockTradeStatusCache.put(stockCode, new StockTradeStatus(buyQty, sellQty, amount));
					log.info("캐시 갱신 - {} buyQty:{} sellQty:{} amount:{}", stockCode, buyQty, sellQty, amount);

				} catch (Exception e) {
					log.error("캐시 갱신 실패 - key: {} error: {}", key, e.getMessage());
				}
			}
		} catch (Exception e) {
			log.error("TradeStats 캐시 전체 갱신 실패: {}", e.getMessage());
		}
	}

	public StockTradeStatus getStats(String stockCode) {
		return stockTradeStatusCache.getOrDefault(stockCode, new StockTradeStatus(0, 0, 0));
	}

	private long parseLong(Object val) {
		return val == null ? 0L : Long.parseLong(val.toString());
	}

	private double parseDouble(Object val) {
		return val == null ? 0.0 : Double.parseDouble(val.toString());
	}
}