package Poi.Stock.features.Websocket;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Order.Order;
import Poi.Stock.features.Order.OrderBook;
import Poi.Stock.util.EnumUtil.tradeType;

@Component
public class OrderBookCache {

	private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

	public OrderBook get(String stockCode) {
		return orderBooks.get(stockCode);
	}

	public void put(String stockCode, OrderBook orderBook) {
		orderBooks.put(stockCode, orderBook);
	}

	public void addOrder(Order savedOrder) {

		OrderBook orderBook = orderBooks.computeIfAbsent(savedOrder.getStockCode(), k -> new OrderBook());

		if (savedOrder.getTradeType() == tradeType.SELL) {
			orderBook.getSellOrders().add(savedOrder);
			orderBook.getSellOrders()
					.sort(Comparator.comparing(Order::getTradePrice).thenComparing(Order::getPriority));
		} else {
			orderBook.getBuyOrders().add(savedOrder);
			orderBook.getBuyOrders()
					.sort(Comparator.comparing(Order::getTradePrice).reversed().thenComparing(Order::getPriority));
		}
	}

	public void removeOrder(Order order) {
		OrderBook book = orderBooks.get(order.getStockCode());
		if (book == null)
			return;
		if (order.getTradeType() == tradeType.BUY) {
			book.getBuyOrders().removeIf(o -> o.getOrderId().equals(order.getOrderId()));
		} else {
			book.getSellOrders().removeIf(o -> o.getOrderId().equals(order.getOrderId()));
		}
	}

}