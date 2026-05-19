package Poi.Stock.features.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "HaveStock")
@Getter
@Setter
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
	private Integer availableQuantity;
	// 평균 매수가
	private double averagePrice;

	public void updateAveragePrice(int qty, int price) {
		if (this.quantity == 0) {
			this.averagePrice = price;
		} else {
			double total = this.averagePrice * this.quantity + (double) price * qty;
			this.averagePrice = total / (this.quantity + qty);
		}
		this.quantity += qty;
	}
}
