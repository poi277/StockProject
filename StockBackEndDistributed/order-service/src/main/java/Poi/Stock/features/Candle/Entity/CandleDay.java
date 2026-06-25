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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candle_day", indexes = { @Index(name = "idx_candle_day_stock_date", columnList = "stockCode, date") })
public class CandleDay implements Candle {
	
	// 일봉 시간 파싱을 위한 포맷터 정의 (예: 20260625)
	private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String stockCode;
	private LocalDate date; // 일 단위 저장 (예: 2026-05-29)

	private Integer open;
	private Integer high;
	private Integer low;
	private Integer close;

	private Long buyQty;
	private Long sellQty;
	private Long totalVolume; // 하루 총 거래량
	private Long tradeAmount; // 하루 총 거래대금

	// 계산 필드 (차트 팝업 툴팁에 필수적인 전일 대비 데이터들)
	private Integer changeAmount; // 전일 종가 대비 등락 절대 금액
	private Double changeRate; // 전일 종가 대비 등락률 (%)

	@Override
	public String getCandleTime() {
		// 일봉은 날짜까지만 유일하면 되므로 yyyy-MM-dd 형식으로 반환
		return this.date != null ? this.date.toString() : "";
	}

	@Override
	public void setCandleTime(String string) {
		if (string == null || string.isBlank()) {
			return;
		}
		try {
			if (string.contains("T")) {
				this.date = LocalDateTime.parse(string).toLocalDate();
			} 
			else if (string.contains("-")) {
				this.date = LocalDate.parse(string);
			}
			else {
				String pureDateStr = string.substring(0, 8);
				this.date = LocalDate.parse(pureDateStr, DAY_FMT);
			}
		} catch (Exception e) {
			log.error("CandleDay - CandleTime 파싱 실패: {}, 에러: {}", string, e.getMessage());
		}
	}
}