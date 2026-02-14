package Poi.Stock.DTO.user;

import Poi.Stock.util.EnumUtil.tradeType;

public class TradeDTO {

	private tradeType tradeType; // BUY / SELL
	String stockId;
	Integer quantity;
	public tradeType getTradeType() {
		return tradeType;
	}
	public void setTradeType(tradeType tradeType) {
		this.tradeType = tradeType;
	}
	public String getStockId() {
		return stockId;
	}
	public void setStockId(String stockId) {
		this.stockId = stockId;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


}


