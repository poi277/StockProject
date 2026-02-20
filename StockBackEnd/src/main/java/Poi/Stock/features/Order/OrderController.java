package Poi.Stock.features.Order;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final OrderRepository orderRepository;

	@PostMapping("/trade")
	public ResponseEntity<ApiResponse> stockTrade(@RequestBody TradeDTO tradeDTO, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "세션에 값이 필요합니다"));
		}
		String userId = authentication.getName();
		// ✅ 주문을 DB에 저장
		Order savedOrder = orderService.createOrder(userId, tradeDTO);
		// 응답 메시지
		String message = String.format("%s 주문 접수 - 종목: %s, 가격: %d원, 수량: %d주 (주문번호: %d)",
				tradeDTO.getTradeType() == tradeType.BUY ? "매수" : "매도", tradeDTO.getStockCode(),
				tradeDTO.getTradePrice(), tradeDTO.getQuantity(), savedOrder.getOrderId());
		return ResponseEntity.ok(new ApiResponse(true, message));
	}

	// ─────────────────────────────────────────────
	// 주문 조회 API 추가
	// ─────────────────────────────────────────────

	@GetMapping("/orders/{stockCode}")
	public ResponseEntity<?> getOrders(@PathVariable("stockCode") String stockCode, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "인증이 필요합니다"));
		}
		String userId = authentication.getName();
		// 내 주문 내역
		List<Order> myOrders = orderRepository.findByUserIdAndStockCodeOrderByCreatedAtDesc(userId, stockCode);
		return ResponseEntity.ok(myOrders);
	}

	@GetMapping("/orderbook/{stockCode}")
	public ResponseEntity<?> getOrderBook(@PathVariable("stockCode") String stockCode) {
		// 호가창 조회
		Map<String, Object> orderBook = orderService.getOrderBook(stockCode);
		return ResponseEntity.ok(orderBook);
	}

}
