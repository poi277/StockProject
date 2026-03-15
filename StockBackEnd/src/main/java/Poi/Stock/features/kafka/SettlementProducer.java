package Poi.Stock.features.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import Poi.Stock.shared.event.SettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void sendSettlement(SettlementEvent event) {
		kafkaTemplate.send("settlement-topic", event.getStockCode(), event);
		log.info("정산 이벤트 발행: stockCode={}, 자산변경={}건, 주식변경={}건", event.getStockCode(), event.getAssetChanges().size(),
				event.getStockChanges().size());
	}
}
