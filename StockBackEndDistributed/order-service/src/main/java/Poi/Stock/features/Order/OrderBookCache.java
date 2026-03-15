package Poi.Stock.features.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OrderBookCache {

	private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

	public OrderBook get(String stockCode) {
		return orderBooks.computeIfAbsent(stockCode, s -> new OrderBook());
	}
	public void put(String stockCode, OrderBook orderBook) {
		orderBooks.put(stockCode, orderBook);
	}
	public void remove(String stockCode) {
		orderBooks.remove(stockCode);
	}
}