package Poi.Stock.DTO.user;

public class SellAndBuyDTO {
	String option;
	String StockId;
	String UserId;
	Integer quantity;

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getStockId() {
		return StockId;
	}

	public void setStockId(String stockId) {
		StockId = stockId;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}


