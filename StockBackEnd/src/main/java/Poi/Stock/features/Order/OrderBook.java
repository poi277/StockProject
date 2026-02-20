package Poi.Stock.features.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderBook {

	private List<Order> sellOrders = new ArrayList<>();
	private List<Order> buyOrders = new ArrayList<>();

	public List<Order> getSellOrders() {
		return sellOrders;
	}

	public void setSellOrders(List<Order> sellOrders) {
		this.sellOrders = sellOrders;
	}

	public List<Order> getBuyOrders() {
		return buyOrders;
	}

	public void setBuyOrders(List<Order> buyOrders) {
		this.buyOrders = buyOrders;
	}


}
