package Poi.Stock.features.Websocket;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.Stock;

@Component
public class StockCache {

	private final Map<String, Stock> stockCache = new ConcurrentHashMap<>();

	public Map<String, Stock> getCache() {
		return stockCache;
	}

	public Stock get(String stockCode) {
		return stockCache.get(stockCode);
	}

	public void put(String stockCode, Stock stock) {
		stockCache.put(stockCode, stock);
	}

	public Collection<Stock> values() {
		return stockCache.values();
	}
}
