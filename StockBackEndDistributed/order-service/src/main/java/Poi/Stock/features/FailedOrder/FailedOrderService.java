package Poi.Stock.features.FailedOrder;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.repository.FailedOrderRepository;
import Poi.Stock.util.EnumUtil.FailStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class FailedOrderService {

	// ✅ 필드는 클래스 레벨에 선언
	private final FailedOrderRepository failedOrderRepository;
	private final WebSocketService webSocketService;

	public void handleFinalFailure(TradeDTO tradeDTO, Exception e) {
		log.error("최종 처리 실패 - userId: {}, stockCode: {}, reason: {}", tradeDTO.getUserId(), tradeDTO.getStockCode(),
				e.getMessage());

		// 1. DB에 실패 이력 저장
		failedOrderRepository.save(FailedOrder.builder().userId(tradeDTO.getUserId()).stockCode(tradeDTO.getStockCode())
				.reason(e.getMessage()).retryCount(3).failedAt(LocalDateTime.now()).status(FailStatus.PENDING_REVIEW)
				.build());

		// 2. 사용자에게 WebSocket 알림
		webSocketService.sendError(tradeDTO.getUserId(), "주문 처리에 실패했습니다.");
	}
}