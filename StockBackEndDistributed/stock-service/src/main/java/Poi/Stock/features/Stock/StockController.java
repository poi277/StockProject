package Poi.Stock.features.Stock;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
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

	@PostMapping("/stocks/info")
	public ResponseEntity<ApiResponse> getStocksByCode(@RequestBody Map<String, List<String>> body) {
		List<String> codes = body.get("codes");
		List<Stock> stocks = stockService.findByCodes(codes);
		return ResponseEntity.ok(new ApiResponse(true, "내가 가지고있는 주식 상세 리스트 반환 완료", stocks));
	}

}
