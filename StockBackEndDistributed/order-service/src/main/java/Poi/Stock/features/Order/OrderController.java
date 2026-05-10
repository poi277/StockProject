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
import Poi.Stock.DTO.user.myAllOrderDTO;
import Poi.Stock.DTO.user.myStockOrderDTO;
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
		// 주문 시작
        orderService.placeOrder(userId, tradeDTO);
        return ResponseEntity.ok(new ApiResponse(true, "주문 접수 완료"));
    }
	// 주문 수정 만들어야함
	@PostMapping("/edit")
	public ResponseEntity<ApiResponse> stockEdit(@RequestBody @Valid TradeDTO tradeDTO,
			Authentication authentication, HttpServletRequest request) {
		System.out.println("ddd");
		String accessToken = resolveToken(request);
		String userId = authentication.getName();
		// 주문도 똑같이 검증
		Order order = orderService.validateEditOrder(userId, tradeDTO, accessToken);
		orderService.stockEdit(tradeDTO, order);
		return ResponseEntity.ok(new ApiResponse(true, "주문 접수 완료"));
	}


    @GetMapping("/orderbook/{stockCode}")
    public ResponseEntity<?> getOrderHoga(@PathVariable("stockCode") String stockCode) {
        Map<String, Object> orderBook = orderService.getOrderHoga(stockCode);
        return ResponseEntity.ok(orderBook);
    }

	@GetMapping("/myorder/{stockCode}")
	public ResponseEntity<?> getMyStockOrders(@PathVariable("stockCode") String stockCode,
			Authentication authentication) {
		String userId = authentication.getName();
		List<myStockOrderDTO> myOrders = orderService.getMyStockOrder(userId, stockCode);
		return ResponseEntity.ok(myOrders);
	}

	@GetMapping("/myallorder")
	public ResponseEntity<ApiResponse> getMyAllStockOrder(Authentication authentication) {
		String userId = authentication.getName();
		List<myAllOrderDTO> data = orderService.getMyAllStockOrder(userId);
		return ResponseEntity.ok(new ApiResponse(true, "조회완료", data));
	}

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable("orderId") Long orderId,
            Authentication authentication,
            HttpServletRequest request) {
		System.out.println(orderId);
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
