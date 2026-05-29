package Poi.Stock.features.Stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // 💡 추가

@Data
@AllArgsConstructor
@NoArgsConstructor // 💡 기본 생성자를 허용하여 'new StockRealTimeSnapshot()'이 가능하게 합니다.
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

	/**
	 * 봇 모듈(OrderService)에서 가격을 안전하게 참조하기 위한 깊은 복사(Deep Copy) 메서드
	 */
	public StockRealTimeSnapshot botCacheCopy() {
		StockRealTimeSnapshot stock = new StockRealTimeSnapshot();

		stock.setStockCode(this.stockCode);
		stock.setStockName(this.stockName);
		stock.setYesterdayClosePrice(this.yesterdayClosePrice);
		stock.setCurrentPrice(this.currentPrice);
		stock.setHighPrice(this.highPrice);
		stock.setLowPrice(this.lowPrice);
		stock.setTotalVolume(this.totalVolume);
		stock.setChangeAmount(this.changeAmount);
		stock.setChangeRate(this.changeRate);

		return stock;
	}

	public int getTickSize(int price) {

		if (price < 1000)
			return 1;
		if (price < 5000)
			return 5;
		if (price < 10000)
			return 10;
		if (price < 50000)
			return 50;
		if (price < 100000)
			return 100;
		if (price < 500000)
			return 500;
		return 1000;
	}
}