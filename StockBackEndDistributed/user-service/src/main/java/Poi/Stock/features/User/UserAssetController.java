package Poi.Stock.features.User;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.features.UserWebsocket.UserWebsocketService;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserAssetController {

    private final UserAssetService userAssetService;
	private final UserWebsocketService userWebsocketService;

    @PostMapping("/validate-order")
	public ResponseEntity<Void> validateOrder(Authentication authentication, @RequestBody Map<String, Object> body) {
        String userId = authentication.getName();
		userAssetService.validateOrder(userId, tradeType.valueOf((String) body.get("tradeType")),
				(String) body.get("stockCode"),
            (Integer) body.get("tradePrice"),
            (Integer) body.get("quantity")
        );
        return ResponseEntity.ok().build();
    }

	@PostMapping("/validate-editOrder")
	public ResponseEntity<Void> validateEditOrder(Authentication authentication,
			@RequestBody Map<String, Object> body) {
		String userId = authentication.getName();
		userAssetService.validateEditOrder(userId, tradeType.valueOf((String) body.get("tradeType")),
				(String) body.get("stockCode"), (Integer) body.get("newPrice"), (Integer) body.get("oldPrice"),
				(Integer) body.get("newQuantity"), (Integer) body.get("RemainingQuantity")
		);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/cancel-reserve")
	public ResponseEntity<Void> cancelReserve(Authentication authentication, @RequestBody Map<String, Object> body) {
        String userId = authentication.getName();
		userAssetService.cancelReserve(userId, tradeType.valueOf((String) body.get("tradeType")),
				(String) body.get("stockCode"), (Integer) body.get("price"), (Integer) body.get("quantity"));
        return ResponseEntity.ok().build();
    }

	@GetMapping("/haveAsset")
	public ResponseEntity<ApiResponse> getUserHaveStock(Authentication authentication) {
		String userId = authentication.getName();
		Map<String, Object> data = userAssetService.userHaveAsset(userId);
		return ResponseEntity.ok(new ApiResponse(true, "조회완료", data));
	}
}
