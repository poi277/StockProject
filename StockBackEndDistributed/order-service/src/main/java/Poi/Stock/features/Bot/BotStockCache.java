package Poi.Stock.features.Bot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.StockRealTimeSnapshot;

@Component
public class BotStockCache {

	private final Map<String, StockRealTimeSnapshot> cache = new ConcurrentHashMap<>();

	public void put(String stockCode, StockRealTimeSnapshot stock) {
		cache.put(stockCode, stock);
	}

	public StockRealTimeSnapshot get(String stockCode) {
		return cache.get(stockCode);
	}
}
