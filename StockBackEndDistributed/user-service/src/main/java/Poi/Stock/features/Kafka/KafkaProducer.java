package Poi.Stock.features.Kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import Poi.Stock.object.SettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

	private final KafkaTemplate<String, SettlementEvent> kafkaTemplate;

	public void sendToSettlementDLT(SettlementEvent tradeExecutionList) {
		kafkaTemplate.send("settlement-topic-DLT", tradeExecutionList);
	}
}