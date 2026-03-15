package Poi.Stock.features.Order;

import java.util.ArrayDeque;
import java.util.Deque;

public class PriceLevel {

	private int totalQuantity;
	private final Deque<Order> orders = new ArrayDeque<>();


	public void addOrder(Order order) {
		orders.addLast(order);
		totalQuantity += order.getRemainingQuantity();
	}

	public void decreaseTopOrderQuantity(int qty) {
		Order top = orders.peekFirst();
		if (top == null)
			return;
		top.decreaseRemainingQuantity(qty); // Order 내부에서 감소
		totalQuantity -= qty;
		if (top.getRemainingQuantity() <= 0) {
			orders.pollFirst();
		}
	}

	public void reduceQuantity(int qty) {
		totalQuantity -= qty;
	}

	public Order peek() {
		return orders.peekFirst();
	}

	public void removeTopOrder() {
		orders.pollFirst();
	}

	public boolean isEmpty() {
		return totalQuantity <= 0 || orders.isEmpty();
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}
	public Deque<Order> getOrders() {
		return orders;
	}

	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

}