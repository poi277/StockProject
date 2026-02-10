package Poi.Stock.features.Stock;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.SellAndBuyDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;

	// 프론트의 apifetch의 반환타입을 위해 ApiResponse으로 반환해야하여 success와 data,message가 표준이다.
	// 웹소켓 가동하는 string을 그냥 보냄 실상은 서비스에서 실행
	@GetMapping("/")
	public Map<String, Object> hello() {
		return Map.of("success", true, "message", "hello websocket");
	}

	// 주식 사거나 팔기
	// @RequestBody에 아이디 정보랑 매수나 매도
	@PostMapping("/SellAndBuy")
	public ResponseEntity<ApiResponse> stockSell(@RequestBody SellAndBuyDTO sellAndBuyDTO) {

		if ("매수".equals(sellAndBuyDTO.getOption())) {
			stockService.buyStock(sellAndBuyDTO.getUserId(), sellAndBuyDTO.getStockId(), sellAndBuyDTO.getQuantity());
			return ResponseEntity.ok(new ApiResponse(true, "매수 완료"));

		} else if ("매도".equals(sellAndBuyDTO.getOption())) {
			stockService.sellStock(sellAndBuyDTO.getUserId(), sellAndBuyDTO.getStockId(), sellAndBuyDTO.getQuantity());
			return ResponseEntity.ok(new ApiResponse(true, "매도 완료"));
		}

		return ResponseEntity.badRequest().body(new ApiResponse(false, "잘못된 옵션입니다"));
	}


}
