package Poi.Stock.DTO.user;

import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public class TradeDTO {
	private String userId;
	@NotBlank
	private String stockCode;
	@NotNull @Positive
	private Integer tradePrice;
	@NotNull @Positive
	private Integer quantity;
	@NotNull
	private tradeType tradeType;

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


