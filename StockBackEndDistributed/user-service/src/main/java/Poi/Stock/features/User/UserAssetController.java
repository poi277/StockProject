package Poi.Stock.features.User;

import Poi.Stock.DTO.user.getAssetDTO;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserAssetController {

    private final UserAssetService userAssetService;

    @GetMapping("/asset")
    public ResponseEntity<getAssetDTO> getMyAsset(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(userAssetService.getMyAsset(userId));
    }

    @PostMapping("/validate-order")
    public ResponseEntity<Void> validateOrder(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        String userId = authentication.getName();
        userAssetService.validateOrder(
            userId,
            tradeType.valueOf((String) body.get("tradeType")),
            (String) body.get("stockCode"),
            (Integer) body.get("tradePrice"),
            (Integer) body.get("quantity")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refund")
    public ResponseEntity<Void> refund(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        String userId = authentication.getName();
        userAssetService.refundAsset(userId, (Integer) body.get("refundAmount"));
        return ResponseEntity.ok().build();
    }
}
