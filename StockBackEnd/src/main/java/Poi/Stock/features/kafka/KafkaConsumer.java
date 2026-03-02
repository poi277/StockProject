package Poi.Stock.features.kafka;

import java.util.Set;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Order.Order;
import Poi.Stock.features.Order.OrderBook;
import Poi.Stock.features.Order.OrderTradeService;
import Poi.Stock.features.Websocket.OrderBookCache;
import Poi.Stock.features.Websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

	private final OrderTradeService orderTradeService;
	private final OrderBookCache orderBookCache;
	private final WebSocketService webSocketService;

	@KafkaListener(topics = "order-topic", groupId = "stock-group")
	public void consumeOrder(@Payload TradeDTO tradeDTO) {
		log.info("카프카 수신: {}", tradeDTO);
		try {
			Order order = orderTradeService.setOrder(tradeDTO.getUserId(), tradeDTO);
			OrderBook book = orderBookCache.get(order.getStockCode());
			Set<Integer> matchedPrices = orderTradeService.processMatching(order, book);
			orderTradeService.sendDeltaForPrice(order.getStockCode(), matchedPrices, book);
			Integer currentPrice = book.getSellfirstKey();
			if (currentPrice != null) {
				webSocketService.SendCurrentPrice(order.getStockCode(), currentPrice);
				orderTradeService.updateStockPrice(order.getStockCode(), currentPrice);
			}
		} catch (Exception e) {
			log.error("주문 처리 실패: {}", e.getMessage());
		}
	}
}