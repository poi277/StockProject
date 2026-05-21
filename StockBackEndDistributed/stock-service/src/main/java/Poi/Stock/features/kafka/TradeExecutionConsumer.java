package Poi.Stock.features.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Stock.StockService;
import Poi.Stock.features.webSocket.WebSocketService;
import Poi.Stock.object.TradeExecutionList;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeExecutionConsumer {

	private final StockCache stockCache;
	private final WebSocketService webSocketService;
	private final StockRepository stockRepository;
	private final StockService stockService;
	private final KafkaProducer kafkaProducer;

	@KafkaListener(topics = "trade-execution-topic", groupId = "stock-service-group")
	@Transactional
	public void consumeTradeExecution(@Payload TradeExecutionList message) {
		try {
			stockService.applyTradeExecutions(message.getExecutions());
		} catch (Exception e) {
			log.error("시세 처리 실패: {}", e.getMessage());
			kafkaProducer.sendToExecutionDLT(message); // DLT로 보내기
		}
	}
	@KafkaListener(topics = "trade-execution-topic-DLT", groupId = "stock-dlt-group")
	public void consumeDLT(@Payload TradeExecutionList message) {
		log.error("DLT 시세 메시지 수신");
		int maxRetry = 3;
		Exception lastException = null;
		for (int attempt = 1; attempt <= maxRetry; attempt++) {
			try {
				stockService.applyTradeExecutions(message.getExecutions());
				log.info("DLT 재처리 성공 - attempt: {}", attempt);
				return;
			} catch (Exception e) {
				lastException = e;
				log.warn("DLT 재시도 실패 [{}/{}]: {}", attempt, maxRetry, e.getMessage());
				if (attempt < maxRetry)
					sleep(attempt);
			}
		}
		// 에러 데이터베이스에 입력
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
