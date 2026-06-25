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
@Table(name = "candle_hour", indexes = { @Index(name = "idx_candle_hour_stock_time", columnList = "stockCode, time") })
public class CandleHour implements Candle {
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String stockCode;
	private LocalDateTime time; // 정시 기준으로 저장 (예: 13:00:00)

	private Integer open;
	private Integer high;
	private Integer low;
	private Integer close;

	private Long buyQty;
	private Long sellQty;
	private Long totalVolume; // buyQty + sellQty (1시간 누적)
	private Long tradeAmount; // 1시간 누적 거래대금

	@Override
	public String getCandleTime() {
		// 분봉은 시분까지 유일해야 하므로 yyyy-MM-dd HH:mm 형식으로 반환
		return this.time.toString();
	}

	@Override
	public void setCandleTime(String string) {
		if (string == null || string.isBlank()) {
			return;
		}

		try {
			if (string.contains("T")) {
				this.time = LocalDateTime.parse(string);
			} else if (string.contains("-") && string.contains(" ")) {
				this.time = LocalDateTime.parse(string.replace(" ", "T"));
			} else {
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