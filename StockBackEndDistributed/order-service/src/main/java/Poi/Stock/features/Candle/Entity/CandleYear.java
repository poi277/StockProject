package Poi.Stock.features.Candle.Entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candle_year", indexes = { @Index(name = "idx_candle_year_stock_date", columnList = "stockCode, date") })
public class CandleYear implements Candle {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String stockCode;
	private LocalDate date;
	private Integer open;
	private Integer high;
	private Integer low;
	private Integer close;
	private Long buyQty;
	private Long sellQty;
	private Long totalVolume;
	private Long tradeAmount;

	@Override
	public String getCandleTime() {
		return this.date.toString();
	}
}