package Poi.Stock.DTO.stock;

import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.features.Stock.StockTradeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockListResponseDto {

	private final StockRealTimeSnapshot snapshot;
	private final StockTradeStatus tradeStatus;
}