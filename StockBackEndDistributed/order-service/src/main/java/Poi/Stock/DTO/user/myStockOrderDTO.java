package Poi.Stock.DTO.user;

import java.time.LocalDateTime;

import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class myStockOrderDTO {
	private Long orderId;
	private tradeType tradeType; // BUY or SELL
	private String stockCode;
	private Integer quantity; // 주문 수량
	private Integer remainingQuantity; // 남은 수량
	private Integer tradePrice; // 주문 가격
	private OrderStatus status; // PENDING, PARTIAL, COMPLETED, CANCELLED
	private LocalDateTime createdAt;
}
