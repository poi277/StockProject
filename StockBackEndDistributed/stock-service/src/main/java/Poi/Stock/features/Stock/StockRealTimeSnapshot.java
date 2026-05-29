package Poi.Stock.features.Stock;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockRealTimeSnapshot {
	private String stockCode;
	private String stockName;
	private int yesterdayClosePrice;
	private int currentPrice; // 현재가 (실시간 변동)
	private int highPrice; // 당일 고가 (실시간 변동)
	private int lowPrice; // 당일 저가 (실시간 변동)
	private long totalVolume; // 당일 누적 거래량 (실시간 변동)
	private int changeAmount; // 당일 등락폭 (실시간 계산)
	private double changeRate; // 당일 등락률 (실시간 계산)
}