package Poi.Stock.features.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.FailedOrder.FailedOrderService;
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
	private final FailedOrderService failedOrderService;

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
		int maxRetry = 3;
		Exception lastException = null;
		for (int attempt = 1; attempt <= maxRetry; attempt++) {
			try {
				orderService.processOrder(tradeDTO);
				log.info("DLT 재처리 성공 - attempt: {}", attempt);
				return;
			} catch (Exception e) {
				lastException = e;
				log.warn("DLT 재시도 실패 [{}/{}]: {}", attempt, maxRetry, e.getMessage());
				if (attempt < maxRetry)
					sleep(attempt);
			}
		}
		failedOrderService.handleFinalFailure(tradeDTO, lastException);
	}
	private void sleep(int attempt) {
		try {
			long waitMs = 1000L * (long) Math.pow(2, attempt - 1);
			log.info("재시도 대기 {}ms", waitMs);
			Thread.sleep(waitMs);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}
}