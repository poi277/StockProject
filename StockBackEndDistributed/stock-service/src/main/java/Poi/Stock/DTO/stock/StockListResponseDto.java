package Poi.Stock.DTO.stock;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockTradeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockListResponseDto {

	private Stock stock;
	private StockTradeStatus tradeStatus;
}