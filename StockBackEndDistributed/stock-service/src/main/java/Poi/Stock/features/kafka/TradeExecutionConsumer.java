package Poi.Stock.features.kafka;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Stock.TradeExecution;
import Poi.Stock.features.webSocket.WebSocketService;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class TradeExecutionConsumer {

	private final StockCache stockCache;
	private final WebSocketService webSocketService;
	private final StockRepository stockRepository;
	private final ObjectMapper objectMapper; // 추가

	@KafkaListener(topics = "trade-execution-topic", groupId = "stock-service-group", containerFactory = "stringKafkaListenerContainerFactory")
	@Transactional
	public void consumeTradeExecution(ConsumerRecord<String, String> record) throws JsonProcessingException {
		List<TradeExecution> executions = objectMapper.readValue(record.value(),
				objectMapper.getTypeFactory().constructCollectionType(List.class, TradeExecution.class));

		String stockCode = executions.get(0).getStockCode();
		Stock stock = stockCache.get(stockCode);
		if (stock == null)
			return;

		for (TradeExecution execution : executions) {
			stock.setClosePrice(execution.getPrice());
			if (stock.getHighPrice() == null || execution.getPrice() > stock.getHighPrice())
				stock.setHighPrice(execution.getPrice());
			if (stock.getLowPrice() == null || execution.getPrice() < stock.getLowPrice())
				stock.setLowPrice(execution.getPrice());
			if (stock.getOpenPrice() != null) {
				stock.setChangeAmount(execution.getPrice() - stock.getOpenPrice());
				stock.setChangeRate(stock.calcChangeRate(execution.getPrice()));
			}
			stock.setTotalvolume(stock.getTotalvolume() + execution.getQuantity());
			webSocketService.sendExecution(execution, stock);
		}

		stockCache.put(stockCode, stock);
		stockRepository.save(stock);
		webSocketService.SendCurrentPrice(stock);
	}
}
