package Poi.Stock.shared.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<StockChange> stockChanges;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetChange {
        private String userId;
        private int delta; // 양수: 증가, 음수: 감소
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockChange {
        private String userId;
        private String stockCode;
        private int quantityDelta; // 양수: 매수, 음수: 매도
        private int fillPrice;     // 체결가 (평균가 계산용)
    }
}