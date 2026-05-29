package Poi.Stock.features.Stock;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class StockCache {

	private final Map<String, StockRealTimeSnapshot> stockCache = new ConcurrentHashMap<>();

	public Map<String, StockRealTimeSnapshot> getCache() {
		return stockCache;
	}

	public StockRealTimeSnapshot get(String stockCode) {
		return stockCache.get(stockCode);
	}

	public void put(String stockCode, StockRealTimeSnapshot stock) {
		stockCache.put(stockCode, stock);
	}

	public Collection<StockRealTimeSnapshot> values() {
		return stockCache.values();
	}
}
