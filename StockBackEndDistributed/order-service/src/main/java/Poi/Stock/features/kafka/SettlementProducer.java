package Poi.Stock.features.kafka;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import Poi.Stock.object.TradeExecution;
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
	}

	public void sendTradeExecutionStockService(List<TradeExecution> executions) {
		kafkaTemplate.send("trade-execution-topic", executions.get(0).getStockCode(), executions);
	}
}