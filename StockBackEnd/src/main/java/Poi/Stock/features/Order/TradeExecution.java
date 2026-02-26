package Poi.Stock.features.Order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeExecution {
	private String buyerId;
	private String sellerId;
	private int quantity;
	private int price;
	private String stockCode;
}