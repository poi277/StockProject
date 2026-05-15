package Poi.Stock.features.Stock;

import lombok.Getter;

@Getter
public class StockTradeStatus {
	private final long buyQuantity;
	private final long sellQuantity;
	private final double tradeAmount;

	public StockTradeStatus(long buyQuantity, long sellQuantity, double tradeAmount) {
        this.buyQuantity = buyQuantity;
        this.sellQuantity = sellQuantity;
        this.tradeAmount = tradeAmount;
    }
}