package Poi.Stock.features.Candle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {

	private final RedisTemplate<String, String> redisTemplate;
	private final StockCache stockCache;
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	public void saveCandle() {
		String time = LocalDateTime.now().format(FMT);
		for (Stock stock : stockCache.values()) {
			String key = "candle:1m:" + stock.getStockCode() + ":" + time;

			Map<String, String> candle = new HashMap<>();
			candle.put("open", String.valueOf(stock.getOpenPrice() != null ? stock.getOpenPrice() : 0));
			candle.put("high", String.valueOf(stock.getHighPrice() != null ? stock.getHighPrice() : 0));
			candle.put("low", String.valueOf(stock.getLowPrice() != null ? stock.getLowPrice() : 0));
			candle.put("close", String.valueOf(stock.getClosePrice() != null ? stock.getClosePrice() : 0));
			candle.put("volume", String.valueOf(stock.getTotalvolume() != null ? stock.getTotalvolume() : 0L));
			candle.put("time", time);

			redisTemplate.opsForHash().putAll(key, candle);
			redisTemplate.expire(key, 7, TimeUnit.DAYS);
		}
		log.info("1분봉 저장 완료: {}개 종목", stockCache.values().size());
	}
}