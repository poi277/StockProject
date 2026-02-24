package Poi.Stock.features.Order;

import java.util.Comparator;
import java.util.TreeMap;

import Poi.Stock.util.EnumUtil.tradeType;

public class OrderBook {

	private final TreeMap<Integer, PriceLevel> buyBook = new TreeMap<>(Comparator.reverseOrder());

	private final TreeMap<Integer, PriceLevel> sellBook = new TreeMap<>();

	public int getLevelQuantity(tradeType side, int price) {
		TreeMap<Integer, PriceLevel> book = side == tradeType.BUY ? buyBook : sellBook;

		PriceLevel level = book.get(price);
		return level == null ? 0 : level.getTotalQuantity();
	}

	public void addOrder(Order order) {

		TreeMap<Integer, PriceLevel> book = order.getTradeType() == tradeType.BUY ? buyBook : sellBook;

		PriceLevel level = book.computeIfAbsent(order.getTradePrice(), PriceLevel::new);

		level.addOrder(order);
	}

	public void removeOrder(Order order) {

		TreeMap<Integer, PriceLevel> book = order.getTradeType() == tradeType.BUY ? buyBook : sellBook;

		PriceLevel level = book.get(order.getTradePrice());
		if (level == null)
			return;

		level.getOrders().removeIf(o -> o.getOrderId().equals(order.getOrderId()));

		if (level.isEmpty()) {
			book.remove(order.getTradePrice());
		}
	}

	public TreeMap<Integer, PriceLevel> getBuyBook() {
		return buyBook;
	}

	public TreeMap<Integer, PriceLevel> getSellBook() {
		return sellBook;
	}

}
