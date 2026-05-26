package Poi.Stock.features.Bot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.Stock;

@Component
public class BotStockCache {

	private final Map<String, Stock> cache = new ConcurrentHashMap<>();

	public void put(String stockCode, Stock stock) {
		cache.put(stockCode, stock);
	}

	public Stock get(String stockCode) {
		return cache.get(stockCode);
	}
}
