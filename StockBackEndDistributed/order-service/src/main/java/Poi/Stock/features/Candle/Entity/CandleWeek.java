package Poi.Stock.features.Candle.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
import lombok.extern.slf4j.Slf4j;

@Entity
@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candle_week", indexes = { @Index(name = "idx_candle_week_stock_date", columnList = "stockCode, date") })
public class CandleWeek implements Candle {

	private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

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

	@Override
	public void setCandleTime(String string) {
		if (string == null || string.isBlank()) {
			return;
		}
		try {
			if (string.contains("T")) {
				this.date = LocalDateTime.parse(string).toLocalDate();
			} else if (string.contains("-")) {
				this.date = LocalDate.parse(string);
			} else {
				String pureDateStr = string.substring(0, 8);
				this.date = LocalDate.parse(pureDateStr, DAY_FMT);
			}
		} catch (Exception e) {
			log.error("CandleDay - CandleTime 파싱 실패: {}, 에러: {}", string, e.getMessage());
		}
	}
}