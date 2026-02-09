package Poi.Stock.features.Stock;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;

	// 프론트의 apifetch의 반환타입을 위해 map으로 반환해야하여 success와 data,message가 표준이다.
	// 웹소켓 가동하는 string을 그냥 보냄 실상은 서비스에서 실행
	@GetMapping("/")
	public Map<String, Object> hello() {
		return Map.of("success", true, "message", "hello websocket");
	}

//	@GetMapping("/{stockId}")
//	public ResponseEntity<Map<String, Object>> stock(@PathVariable("stockId") String stockId) {
//		Optional<Stock> stock = stockService.getStock(stockId);
//		return ResponseEntity.ok(Map.of("success", true, "data", stock));
//	}

	// 주식 사거나 팔기
	@PostMapping("/{stockId}")
	public ResponseEntity<Map<String, Object>> stockSell(@PathVariable("stockId") String stockId) {
		return ResponseEntity.ok(Map.of("success", true, "data", stockId));
	}

}
