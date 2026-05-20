package Poi.Stock.features.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.webSocket.WebSocketService;
import Poi.Stock.object.TradeExecution;
import Poi.Stock.object.TradeExecutionList;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class TradeExecutionConsumer {

	private final StockCache stockCache;
	private final WebSocketService webSocketService;
	private final StockRepository stockRepository;
	private final ObjectMapper objectMapper; // 추가

	@KafkaListener(topics = "trade-execution-topic", groupId = "stock-service-group")
	@Transactional
	public void consumeTradeExecution(@Payload TradeExecution message) {
		List<TradeExecutionList> executions = message.getTradeExecutionList();

		String stockCode = executions.get(0).getStockCode();
		Stock stock = stockCache.get(stockCode);
		if (stock == null)
			return;

		for (TradeExecutionList execution : executions) {
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
