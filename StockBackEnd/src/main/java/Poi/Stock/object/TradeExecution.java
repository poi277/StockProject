package Poi.Stock.object;

import Poi.Stock.util.EnumUtil.tradeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeExecution {
	private tradeType tradeType;
	private String buyerId;
	private String sellerId;
	private int quantity;
	private int price;
	private String stockCode;
}