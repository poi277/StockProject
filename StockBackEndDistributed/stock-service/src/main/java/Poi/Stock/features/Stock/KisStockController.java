package Poi.Stock.features.Stock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.config.kis.AskingPriceResponse;
import Poi.Stock.config.kis.CurrentPriceResponse;
import Poi.Stock.config.kis.KisStockService;

@RestController
@RequestMapping("/api/kis/stock")
public class KisStockController {

	private final KisStockService kisStockService;

	public KisStockController(KisStockService kisStockService) {
		this.kisStockService = kisStockService;
	}

	@GetMapping("/price/{code}")
	public CurrentPriceResponse price(@PathVariable("code") String code) {
		return kisStockService.getCurrentPrice(code);
	}

	@GetMapping("/ask/{code}")
	public AskingPriceResponse ask(@PathVariable("code") String code) {
		return kisStockService.getAskingPrice(code);
	}

}