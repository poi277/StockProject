package Poi.Stock.features.Stock;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.getAssetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/stocklist")
    public ResponseEntity<ApiResponse> stockList() {
        List<Stock> stocklist = stockService.getAllStocks();
        return ResponseEntity.ok(new ApiResponse(true, "리스트 불러오기 완료", stocklist));
    }

    @GetMapping("/{stockId}")
    public ResponseEntity<ApiResponse> getStock(@PathVariable("stockId") String stockId) {
        Stock stock = stockService.getStock(stockId);
        Map<String, Object> data = new HashMap<>();
        data.put("stock", stock);
        return ResponseEntity.ok(new ApiResponse(true, "주식 불러오기 완료", data));
    }

    @GetMapping("/myAsset")
    public ResponseEntity<ApiResponse> getMyAsset(HttpServletRequest request) {
        // Authorization 헤더에서 토큰 추출 후 user-service로 포워딩
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;

        getAssetDTO asset = stockService.getMyAsset(token);
        return ResponseEntity.ok(new ApiResponse(true, "자산 불러오기 완료", asset));
    }
}
