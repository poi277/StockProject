package Poi.Stock.DTO.user;

import Poi.Stock.util.EnumUtil.tradeType;

public class TradeDTO {

	private tradeType tradeType; // BUY / SELL
	String stockCode;
	Integer quantity;
	Integer tradePrice;
	private String userId;

	public tradeType getTradeType() {
		return tradeType;
	}
	public void setTradeType(tradeType tradeType) {
		this.tradeType = tradeType;
	}

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getTradePrice() {
		return tradePrice;
	}

	public void setTradePrice(Integer tradePrice) {
		this.tradePrice = tradePrice;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}


