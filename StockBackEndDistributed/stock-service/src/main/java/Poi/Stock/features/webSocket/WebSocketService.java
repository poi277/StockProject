package Poi.Stock.features.webSocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.object.TradeExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketService {
	private final SimpMessagingTemplate messagingTemplate;

	public void sendCurrentPrice(StockRealTimeSnapshot snapshot) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("stockCode", snapshot.getStockCode());
		payload.put("currentPrice", snapshot.getCurrentPrice());
		payload.put("highPrice", snapshot.getHighPrice());
		payload.put("lowPrice", snapshot.getLowPrice());
		payload.put("totalVolume", snapshot.getTotalVolume());
		payload.put("changeAmount", snapshot.getChangeAmount());
		payload.put("changeRate", snapshot.getChangeRate());
		// log.info("현재가 전송 - stockCode: {}, payload: {}", snapshot.getStockCode(),
		// payload);
		messagingTemplate.convertAndSend("/topic/stock/" + snapshot.getStockCode(), payload);
	}


	public void sendExecution(TradeExecution execution, Integer yesterdayClosePrice, Long totalVolume) {

		double changeRate = 0.0;

		if (yesterdayClosePrice != null && yesterdayClosePrice != 0) {
			changeRate = (double) (execution.getPrice() - yesterdayClosePrice) / yesterdayClosePrice * 100;
		}

		Map<String, Object> payload = new HashMap<>();
		payload.put("tradeType", execution.getTradeType());
		payload.put("price", execution.getPrice());
		payload.put("quantity", execution.getQuantity());
		payload.put("changeRate", changeRate);
		payload.put("totalVolume", totalVolume);
		payload.put("time", execution.getTime().toString());

		// log.info("체결 전송 - stockCode: {}, payload: {}", execution.getStockCode(),
		// payload);

		messagingTemplate.convertAndSend("/topic/execution/" + execution.getStockCode(), payload);
	}
}
