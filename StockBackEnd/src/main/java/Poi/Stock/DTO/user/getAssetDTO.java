package Poi.Stock.DTO.user;

import java.util.List;

public class getAssetDTO {
	private Integer totalAsset; // 보유 현금
	private List<HoldingDTO> holdings; // 보유 주식 목록

	public getAssetDTO() {
	}

	public getAssetDTO(Integer totalAsset, List<HoldingDTO> holdings) {
		this.totalAsset = totalAsset;
		this.holdings = holdings;
	}

	public Integer getTotalAsset() {
		return totalAsset;
	}

	public void setTotalAsset(Integer totalAsset) {
		this.totalAsset = totalAsset;
	}

	public List<HoldingDTO> getHoldings() {
		return holdings;
	}

	public void setHoldings(List<HoldingDTO> holdings) {
		this.holdings = holdings;
	}

	// 보유 주식 정보를 담을 내부 DTO
	public static class HoldingDTO {
		private String stockCode;
		private Integer quantity;
		private Integer averagePrice;

		public HoldingDTO() {
		}

		public HoldingDTO(String stockCode, Integer quantity, Integer averagePrice) {
			this.stockCode = stockCode;
			this.quantity = quantity;
			this.averagePrice = averagePrice;
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

		public Integer getAveragePrice() {
			return averagePrice;
		}

		public void setAveragePrice(Integer averagePrice) {
			this.averagePrice = averagePrice;
		}
	}
}