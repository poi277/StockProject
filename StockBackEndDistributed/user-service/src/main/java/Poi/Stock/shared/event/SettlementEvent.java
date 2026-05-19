package Poi.Stock.shared.event;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 체결 후 자산/보유주식 정산 이벤트
 * 토픽: settlement-topic
 * 발행: OrderTradeService (order 로직)
 * 소비: SettlementConsumer (user 로직)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEvent {
	private String stockCode;
	private List<haveStockChange> stockChanges;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class haveStockChange {
		private String userId;
		private int tradeQuantity;
		private int tradePrice;
	}
}