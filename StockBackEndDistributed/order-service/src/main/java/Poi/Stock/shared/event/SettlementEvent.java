package Poi.Stock.shared.event;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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