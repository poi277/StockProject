package Poi.Stock.features.CompletedOrder;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/completed")
@RequiredArgsConstructor
public class CompletedOrderController {

	private final CompletedOrderService completedOrderService;

	@GetMapping("/order")
	public ResponseEntity<ApiResponse> getUserCompletedOrder(Authentication authentication) {
		String userId = authentication.getName();
		List<CompletedOrder> orders = completedOrderService.getUserCompletedOrders(userId);
		return ResponseEntity.ok(new ApiResponse(true, "조회 완료", orders));
	}
}
