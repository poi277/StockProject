package Poi.Stock.features.TradeHistory;

import java.time.LocalDateTime;

import Poi.Stock.features.Order.TradeExecution;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
@Entity
@Getter
@Table(name = "trade_history")
public class TradeHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String buyerId; // FK 아닌 참조용
	private String sellerId; // FK 아닌 참조용
	private String stockCode;
	private int quantity;
	private int price;
	private LocalDateTime tradedAt;

	public static TradeHistory from(TradeExecution ex) {
		TradeHistory h = new TradeHistory();
		h.buyerId = ex.getBuyerId();
		h.sellerId = ex.getSellerId();
		h.stockCode = ex.getStockCode();
		h.quantity = ex.getQuantity();
		h.price = ex.getPrice();
		h.tradedAt = LocalDateTime.now();
		return h;
	}
}
