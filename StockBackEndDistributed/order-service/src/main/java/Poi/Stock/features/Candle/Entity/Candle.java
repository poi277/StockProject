package Poi.Stock.features.Candle.Entity;

public interface Candle {
	Integer getOpen();

	Integer getHigh();

	Integer getLow();

	Integer getClose();

	Long getBuyQty();

	Long getSellQty();

	Long getTotalVolume();

	Long getTradeAmount();
}