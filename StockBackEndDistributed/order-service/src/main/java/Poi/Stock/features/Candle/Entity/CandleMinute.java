package Poi.Stock.features.Candle.Entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candle_minute", indexes = { @Index(name = "idx_candle_stock_time", columnList = "stockCode, time") })
public class CandleMinute implements Candle {

	// 🎯 분봉용 포맷터 정의 (예: 202606252214)
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String stockCode;
	private LocalDateTime time;
	private Integer open;
	private Integer high;
	private Integer low;
	private Integer close;

	private Long buyQty;
	private Long sellQty;
	private Long totalVolume; // buyQty + sellQty 집계 및 60분봉 변환을 위해 추가
	private Long tradeAmount;

	@Override
	public String getCandleTime() {
		return this.time != null ? this.time.toString() : "";
	}
	@Override
	public void setCandleTime(String string) {
		if (string == null || string.isBlank()) {
			return;
		}

		try {
			if (string.contains("T")) {
				this.time = LocalDateTime.parse(string);
			}
			else if (string.contains("-") && string.contains(" ")) {
				this.time = LocalDateTime.parse(string.replace(" ", "T"));
			}
			else {
				String pureTimeStr = string.replaceAll("[^0-9]", "");
				if (pureTimeStr.length() >= 12) {
					pureTimeStr = pureTimeStr.substring(0, 12);
				}
				this.time = LocalDateTime.parse(pureTimeStr, FMT);
			}
		} catch (Exception e) {
			log.error("CandleMinute - CandleTime 파싱 실패: {}, 에러: {}", string, e.getMessage());
		}
	}
}