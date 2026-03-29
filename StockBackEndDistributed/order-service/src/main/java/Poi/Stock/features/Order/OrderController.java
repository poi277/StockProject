package Poi.Stock.features.Order;

import java.util.List;
import java.util.Map;

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
import Poi.Stock.DTO.user.myOrderDTO;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderBookCache orderBookCache;
    private final WebSocketService webSocketService;
	private final OrderCancelService orderCancelService;

    @PostMapping("/trade")
    public ResponseEntity<ApiResponse> stockTrade(
			@RequestBody @Valid TradeDTO tradeDTO,
            Authentication authentication,
            HttpServletRequest request) {
        String userId = authentication.getName();
        String accessToken = resolveToken(request);
        // user-service로 검증
        orderService.validateOrder(userId, tradeDTO, accessToken);
        orderService.placeOrder(userId, tradeDTO);
        return ResponseEntity.ok(new ApiResponse(true, "주문 접수 완료"));
    }

    @GetMapping("/orders/{stockCode}")
	public ResponseEntity<?> getOrders(
            @PathVariable("stockCode") String stockCode,
            Authentication authentication) {
        String userId = authentication.getName();
        List<Order> myOrders = orderRepository
            .findByUserIdAndStockCodeOrderByCreatedAtDesc(userId, stockCode);
        return ResponseEntity.ok(myOrders);
    }

    @GetMapping("/orderbook/{stockCode}")
    public ResponseEntity<?> getOrderHoga(@PathVariable("stockCode") String stockCode) {
        Map<String, Object> orderBook = orderService.getOrderHoga(stockCode);
        return ResponseEntity.ok(orderBook);
    }

	@GetMapping("/myorder")
	public ResponseEntity<ApiResponse> getMyOrder(Authentication authentication) {
		String userId = authentication.getName();
		List<myOrderDTO> data = orderService.getMyOrder(userId);
		return ResponseEntity.ok(new ApiResponse(true, "조회완료", data));
	}

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable("orderId") Long orderId,
            Authentication authentication,
            HttpServletRequest request) {
        String userId = authentication.getName();
        String accessToken = resolveToken(request);
		orderCancelService.cancelOrder(userId, orderId, accessToken);
        return ResponseEntity.ok(new ApiResponse(true, "주문 취소 완료"));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
