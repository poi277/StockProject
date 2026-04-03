package Poi.Stock.features.Candle;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/candle")
public class CandleController {

	private final CandleService candleService;

	@GetMapping("/{stockCode}")
	public ResponseEntity<List<CandleDTO>> getCandle(@PathVariable("stockCode") String stockCode,
			@RequestParam(name = "type", defaultValue = "ONE_MINUTE") CandleType type,
			@RequestParam(name = "startTime", required = false) String startTime,
			@RequestParam(name = "endTime", required = false) String endTime) {
		List<CandleDTO> data = candleService.getCandle(type, stockCode, startTime, endTime);
		return ResponseEntity.ok(data);
	}
}