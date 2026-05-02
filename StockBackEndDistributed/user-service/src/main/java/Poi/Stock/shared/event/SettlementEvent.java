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
    private List<AssetChange> assetChanges;
    private List<haveStockChange> stockChanges;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetChange {
        private String userId;
		private int tradeMoney; // 양수: 증가, 음수: 감소
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
	public static class haveStockChange {
        private String userId;
        private String stockCode;
		private int tradeQuantity; // 양수: 매수, 음수: 매도
		private int tradePrice; // 체결가 (평균가 계산용)
    }
}