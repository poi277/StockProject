package Poi.Stock.features.Stock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.getAssetDTO;
import Poi.Stock.features.WatchList.WatchListService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;
	private final WatchListService watchListService;

	@GetMapping("/stocklist")
	public ResponseEntity<ApiResponse> stockList() {
		List<Stock> stocklist = stockService.getAllStocks();
		return ResponseEntity.ok(new ApiResponse(true, "리스트 불러오기 완료", stocklist));
	}

	@GetMapping("/{stockId}")
	public ResponseEntity<ApiResponse> getStock(@PathVariable("stockId") String stockId,
			Authentication authentication) {
		Stock stock = stockService.getStock(stockId);
		boolean watched = false;
		if (authentication != null) {
			watched = watchListService.isWatched(authentication.getName(), stockId);
		}
		Map<String, Object> data = new HashMap<>();
		data.put("stock", stock);
		data.put("watched", watched);
		return ResponseEntity.ok(new ApiResponse(true, "주식 한개 불러오기 완료", data));
	}

	@GetMapping("/myAsset")
	public ResponseEntity<ApiResponse> getMyAsset() {
		getAssetDTO getAssetDTO = stockService.getMyAsset();
		return ResponseEntity.ok(new ApiResponse(true, "자산 불러오기 완료", getAssetDTO));
	}

}
