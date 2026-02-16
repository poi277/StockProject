package Poi.Stock.features.Stock;

import java.util.List;

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
import Poi.Stock.DTO.user.getAssetDTO;
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

	// pathvariable 오류로 ("stockId")를 붙어야함
	@GetMapping("/{stockId}")
	public ResponseEntity<ApiResponse> getStock(@PathVariable("stockId") String stockId) {
		Stock stock = stockService.getStock(stockId);
		return ResponseEntity.ok(new ApiResponse(true, "주식 한개 불러오기 완료", stock));
	}

	// 주식 사거나 팔기
	// @RequestBody에 아이디 정보랑 매수나 매도
	@PostMapping("/trade")
	public ResponseEntity<ApiResponse> stockSell(@RequestBody TradeDTO tradeDTO, Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "세션에 값이 필요합니다"));
		}
		// ✅ 세션에서 인증된 사용자 ID 추출
		String userId = authentication.getName();
		switch (tradeDTO.getTradeType()) {
		case BUY -> {
			stockService.buyStock(userId, tradeDTO.getStockId(), tradeDTO.getQuantity());
			return ResponseEntity.ok(new ApiResponse(true, "매수 완료"));
		}
		case SELL -> {
			stockService.sellStock(userId, tradeDTO.getStockId(), tradeDTO.getQuantity());
			return ResponseEntity.ok(new ApiResponse(true, "매도 완료"));
		}
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "잘못된 옵션입니다"));
	}
	@GetMapping("/myAsset")
	public ResponseEntity<ApiResponse> getMyAsset() {
		getAssetDTO getAssetDTO = stockService.getMyAsset();
		return ResponseEntity.ok(new ApiResponse(true, "자산 불러오기 완료", getAssetDTO));
	}

}
