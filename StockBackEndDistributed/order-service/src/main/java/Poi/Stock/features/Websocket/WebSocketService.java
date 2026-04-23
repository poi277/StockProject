package Poi.Stock.features.Websocket;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.object.TradeExecution;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

	private final StockRepository stockRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final StockCache stockCache;
	private final OrderBookCache orderBookCache;
	private final OrderRepository orderRepository;
	// 웹소켓을 위해 메모리에 주식 정보 저장 (종목코드별 최신 데이터)

//	-------------------------------
	// 가격 업데이트 및 WebSocket 전송
	public void SendCurrentPrice(Stock stock, LocalDateTime tradeTime) {
		Integer currentPrice = stock.getClosePrice();
		if (currentPrice == null || currentPrice <= 0)
			return;
		Map<String, Object> payload = new HashMap<>();
		payload.put("stockCode", stock.getStockCode());
		payload.put("currentPrice", stock.getClosePrice());
		payload.put("tradeTime", tradeTime);
		payload.put("changeRate", stock.getChangeRate());
		payload.put("changeAmount", stock.getChangeAmount());
		System.out.println(payload);
		messagingTemplate.convertAndSend("/topic/stock/" + stock.getStockCode(), payload);
	}

	public void sendHoga(String stockCode, tradeType side, int price, int qty) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("type", "hoga");
		payload.put("side", side.name());
		payload.put("price", price);
		payload.put("qty", qty);

		System.out.println("=== WebSocket 전송 ===");
		System.out.println("destination: /topic/hoga/" + stockCode);
		System.out.println("payload: " + payload);

		messagingTemplate.convertAndSend("/topic/hoga/" + stockCode, payload);
	}

	public void sendExecution(String stockCode, TradeExecution execution) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("tradeType", execution.getTradeType());
		payload.put("price", execution.getPrice());
		payload.put("quantity", execution.getQuantity());
		payload.put("changeRate", execution.getChangeRate());
		payload.put("totalVolume", execution.getTotalVolume());
		payload.put("time", execution.getTime().toString());
		log.info("체결 전송 - stockCode: {}, payload: {}", stockCode, payload);
		messagingTemplate.convertAndSend("/topic/execution/" + stockCode, payload);
	}

	public void sendError(String userId, String message) {
		System.out.println(message);
		messagingTemplate.convertAndSend("/topic/error/" + userId, message);
	}

	public void sendCurrentCandle(CandleDTO candleDTO, String stockCode) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("open", candleDTO.getOpen());
		payload.put("low", candleDTO.getLow());
		payload.put("high", candleDTO.getHigh());
		payload.put("close", candleDTO.getClose());
		payload.put("volume", candleDTO.getVolume());
		payload.put("time", candleDTO.getTime());
		System.out.println(payload);
		messagingTemplate.convertAndSend("/topic/candle/" + stockCode, payload);
	}

}
