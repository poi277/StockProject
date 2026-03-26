package Poi.Stock.features.Order;

import java.time.LocalDateTime;

import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;

	private String userId; // 주문한 사용자
	private String stockCode; // 종목코드
	private tradeType tradeType; // BUY or SELL
	private Integer quantity; // 주문 수량
	private Integer remainingQuantity; // 남은 수량
	private Integer tradePrice; // 주문 가격
	private OrderStatus status; // PENDING, PARTIAL, COMPLETED, CANCELLED
	private LocalDateTime createdAt;
	private Long priority; // 시간 우선 순위

	public void decreaseRemainingQuantity(int qty) {
		if (qty <= 0)
			return;
		if (this.remainingQuantity < qty) {
			throw new IllegalArgumentException("감소 수량이 남은 수량보다 큽니다.");
		}
		this.remainingQuantity -= qty;
		if (this.remainingQuantity == 0) {
			this.status = OrderStatus.COMPLETED;
		} else {
			this.status = OrderStatus.PARTIAL;
		}
	}

	public boolean isCompleted() {
		return this.status == OrderStatus.COMPLETED;
	}
}