package Poi.Stock.features.webSocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.TradeExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketService {
	private final SimpMessagingTemplate messagingTemplate;

	public void SendCurrentPrice(Stock stock) {

		Integer currentPrice = stock.getClosePrice();
		if (currentPrice == null || currentPrice <= 0)
			return;
		Map<String, Object> payload = new HashMap<>();
		payload.put("stockCode", stock.getStockCode());
		payload.put("currentPrice", stock.getClosePrice());
		payload.put("changeRate", stock.getChangeRate());
		payload.put("changeAmount", stock.getChangeAmount());
		System.out.println(payload);
		messagingTemplate.convertAndSend("/topic/stock/" + stock.getStockCode(), payload);
	}

	public void sendExecution(TradeExecution execution, Stock stock) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("tradeType", execution.getTradeType());
		payload.put("price", execution.getPrice());
		payload.put("quantity", execution.getQuantity());
		payload.put("changeRate", stock.getChangeRate());
		payload.put("totalVolume", stock.getTotalvolume());
		payload.put("time", execution.getTime().toString());
		log.info("체결 전송 - stockCode: {}, payload: {}", execution.getStockCode(), payload);
		messagingTemplate.convertAndSend("/topic/execution/" + execution.getStockCode(), payload);
	}

}
