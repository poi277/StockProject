package Poi.Stock.features.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Lock.StockLock;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.features.Order.OrderService;
import Poi.Stock.features.Order.OrderTradeService;
import Poi.Stock.features.Websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

	private final OrderTradeService orderTradeService;
	private final OrderService orderService;
	private final OrderBookCache orderBookCache;
	private final WebSocketService webSocketService;
	private final StockLock stockLock;
	private final KafkaProducer kafkaProducer;
	@KafkaListener(topics = "order-topic", groupId = "stock-group")
	public void consumeOrder(@Payload TradeDTO tradeDTO) {
		stockLock.lock(tradeDTO.getStockCode());
		try {
			orderService.processOrder(tradeDTO);
		} catch (Exception e) {
			log.error("주문 처리 실패: {}", e.getMessage());
			kafkaProducer.sendToDLT(tradeDTO); // DLT로 보내기
		} finally {
			stockLock.unlock(tradeDTO.getStockCode());
		}
	}

	@KafkaListener(topics = "order-topic.DLT", groupId = "stock-dlt-group")
	public void consumeDLT(@Payload TradeDTO tradeDTO) {
		log.error("DLT 메시지 수신 - userId: {}, stockCode: {}", tradeDTO.getUserId(), tradeDTO.getStockCode());
		// 재 처리 로직
	}
}