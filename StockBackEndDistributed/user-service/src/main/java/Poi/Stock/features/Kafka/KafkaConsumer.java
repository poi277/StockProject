// SettlementConsumer.java
package Poi.Stock.features.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.features.User.UserAssetService;
import Poi.Stock.object.SettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

	private final UserAssetService userAssetService;
	private final KafkaProducer kafkaProducer;

	@KafkaListener(topics = "settlement-topic", groupId = "settlement-group")
	@Transactional
	public void consume(@Payload SettlementEvent event) {
		log.info("정산 이벤트 수신: {}", event);
		try {
			userAssetService.applySettlement(event);
		} catch (Exception e) {
			log.error("정산 처리 실패: {}", e.getMessage(), e);
			kafkaProducer.sendToSettlementDLT(event); // DLT로 보내기
		}
	}

	@KafkaListener(topics = "order-topic.DLT", groupId = "stock-dlt-group")
	public void consumeDLT(@Payload SettlementEvent event) {
		log.error("정산 DLT 메시지 수신 ");
		int maxRetry = 3;
		Exception lastException = null;
		for (int attempt = 1; attempt <= maxRetry; attempt++) {
			try {
				userAssetService.applySettlement(event);
				log.info("DLT 재처리 성공 - attempt: {}", attempt);
				return;
			} catch (Exception e) {
				lastException = e;
				log.warn("DLT 재시도 실패 [{}/{}]: {}", attempt, maxRetry, e.getMessage());
				if (attempt < maxRetry)
					sleep(attempt);
			}
		}
		// failedOrderService.handleFinalFailure(tradeDTO, lastException);
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