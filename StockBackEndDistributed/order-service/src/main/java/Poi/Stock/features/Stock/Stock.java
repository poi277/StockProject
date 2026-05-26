package Poi.Stock.features.Stock;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Stock",indexes = {
        // 날짜만으로 조회할 경우를 위한 인덱스
        @Index(name = "idx_date", columnList = "date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StockDailyPriceId.class)
public class Stock {
	@Id
	private String stockCode;
	@Id
	private LocalDate date;
	private String stockName;
	private Integer openPrice; // 시가
	private Integer highPrice; // 고가
	private Integer lowPrice; // 저가
	private Integer closePrice; // 종가
	private Long totalvolume; // 거래량
	private Long value; // 거래대금
	// 계산 필드들 (선택사항)
	// 거래량
	private Integer changeAmount;
	private Double changeRate;

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

	public double calcChangeRate(int price) {
		if (openPrice == null || openPrice == 0)
			return 0.0;
		double rate = (double) (price - openPrice) / openPrice * 100;
		return Math.round(rate * 100.0) / 100.0;
	}

	public long fillTotalvolume(int fillVolume) {
		return totalvolume + fillVolume;
	}
	
	public Stock botCacheCopy() {
		Stock stock = new Stock();

		stock.setStockCode(this.stockCode);
		stock.setOpenPrice(this.openPrice);
		stock.setClosePrice(this.closePrice);
		stock.setHighPrice(this.highPrice);
		stock.setLowPrice(this.lowPrice);
		return stock;
	}
}