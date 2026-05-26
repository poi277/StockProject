package Poi.Stock.features.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import Poi.Stock.object.TradeExecutionList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

	private final KafkaTemplate<String, TradeExecutionList> kafkaTemplate;

	public void sendToExecutionDLT(TradeExecutionList tradeExecutionSummary) {
		kafkaTemplate.send("trade-execution-topic-DLT", tradeExecutionSummary);
	}
}