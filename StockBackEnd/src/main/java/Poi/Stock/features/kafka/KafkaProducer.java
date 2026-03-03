package Poi.Stock.features.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import Poi.Stock.DTO.user.TradeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

	private final KafkaTemplate<String, TradeDTO> kafkaTemplate;

	public void sendOrder(TradeDTO tradeDTO) {
		kafkaTemplate.send("order-topic", tradeDTO.getStockCode(), tradeDTO);
	}

	public void sendToDLT(TradeDTO tradeDTO) {
		kafkaTemplate.send("order-topic.DLT", tradeDTO);
	}
}