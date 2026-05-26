package Poi.Stock.features.Websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.features.Bot.Bot;
import Poi.Stock.features.Bot.BotCache;
import Poi.Stock.features.Order.Order;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

	private final StockRepository stockRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final OrderBookCache orderBookCache;
	private final OrderRepository orderRepository;
	private final BotCache botCache;
	// 웹소켓을 위해 메모리에 주식 정보 저장 (종목코드별 최신 데이터)

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
	public void sendCurrentCandle(CandleDTO candleDTO, String stockCode) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("open", candleDTO.getOpen());
		payload.put("low", candleDTO.getLow());
		payload.put("high", candleDTO.getHigh());
		payload.put("close", candleDTO.getClose());
		payload.put("buyQty", candleDTO.getBuyQty());
		payload.put("sellQty", candleDTO.getBuyQty());
		payload.put("time", candleDTO.getTime());
		System.out.println(payload);
		messagingTemplate.convertAndSend("/topic/candle/" + stockCode, payload);
	}

	public void sendOrderUpdate(MatchingResult result) {
	    for (Order order : result.getCompletedResting()) {
	        if (isBot(order.getUserId())) continue;
			sendToUser(order.getUserId(), order, order.getStatus());
	    }
	    for (Order order : result.getPartialResting()) {
	        if (isBot(order.getUserId())) continue;
			sendToUser(order.getUserId(), order, order.getStatus());
	    }
	    Order incoming = result.getIncomingOrder();
	    if (incoming != null && !isBot(incoming.getUserId())) {
			sendToUser(incoming.getUserId(), incoming, incoming.getStatus());
	    }
	}

	public void sendToUser(String userId, Order order, OrderStatus orderStatus) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("orderId", order.getOrderId());
		payload.put("stockCode", order.getStockCode());
		payload.put("stockName", order.getStockName());
		payload.put("tradeType", order.getTradeType());
		payload.put("quantity", order.getQuantity());
		payload.put("remainingQuantity", order.getRemainingQuantity());
		payload.put("tradePrice", order.getTradePrice());
		payload.put("status", orderStatus);

		log.info(
				"Order Update Send -> userId: {}, orderId: {}, stockCode: {}, status: {}, quantity: {}, remainingQuantity: {}, price: {}",
				userId, order.getOrderId(), order.getStockCode(), orderStatus, order.getQuantity(),
				order.getRemainingQuantity(), order.getTradePrice());

		messagingTemplate.convertAndSendToUser(userId, "/queue/orders", payload);
	}

	public void sendError(String userId, String message) {
		System.out.println(message);
		messagingTemplate.convertAndSend("/topic/error/" + userId, message);
	}


	private boolean isBot(String userId) {
		Bot bot = botCache.get(userId);
		return bot != null && bot.getBotType() != null;
	}

}
