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