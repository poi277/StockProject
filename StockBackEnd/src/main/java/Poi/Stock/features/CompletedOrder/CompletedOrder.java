package Poi.Stock.features.CompletedOrder;

import java.time.LocalDateTime;

import Poi.Stock.features.Order.Order;
import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "completed_orders")
@Getter
@NoArgsConstructor
public class CompletedOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long orderId; // 원본 주문 ID
	private String stockCode;
	private String userId;
	private int tradePrice;
	private int quantity;

	@Enumerated(EnumType.STRING)
	private tradeType tradeType; // BUY or SELL

	private LocalDateTime completedAt;

	public static CompletedOrder setCompletedOrder(Order order) {
		CompletedOrder co = new CompletedOrder();
		co.orderId = order.getOrderId();
		co.stockCode = order.getStockCode();
		co.userId = order.getUserId();
		co.tradePrice = order.getTradePrice();
		co.quantity = order.getQuantity();
		co.tradeType = order.getTradeType();
		co.completedAt = LocalDateTime.now();
		return co;
	}
}