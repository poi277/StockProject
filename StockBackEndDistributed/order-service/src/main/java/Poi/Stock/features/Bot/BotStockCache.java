package Poi.Stock.features.Bot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.StockRealTimeSnapshot;

//이 캐시는 봇들이 사용하기위한 stockcache입니다.
//안에 stock정보가 담겨있으며 이를 이용하여 봇들이 매수나 매도를 결정합니다.
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
