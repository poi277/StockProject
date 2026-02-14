package Poi.Stock.features.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "HaveStock")
public class HaveStock {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	// 어떤 사용자가
	@ManyToOne
	@JoinColumn(name = "stockUser_id")
	private StockUser stockUser;
	// 어떤 주식을
	private String stockCode; // "005930" (삼성전자)
	// 몇 주 보유하고 있는지
	private Integer quantity;
	// 평균 매수가
	private Integer averagePrice;

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public StockUser getStockUser() {
		return stockUser;
	}

	public void setStockUser(StockUser stockUser) {
		this.stockUser = stockUser;
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
