package Poi.Stock.features.Stock;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.getAssetDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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
		return ResponseEntity.ok(new ApiResponse(true, "주식 불러오기 완료", stock));
    }

	// 이는 userserivce로 보냄
	@GetMapping("/watch/{stockId}")
	public ResponseEntity<Stock> getWatchStock(@PathVariable("stockId") String stockId) {
		Stock stock = stockService.getStock(stockId);
		return ResponseEntity.ok(stock);
	}

    @GetMapping("/myAsset")
    public ResponseEntity<ApiResponse> getMyAsset(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;

        getAssetDTO asset = stockService.getMyAsset(token);
        return ResponseEntity.ok(new ApiResponse(true, "자산 불러오기 완료", asset));
    }
}
