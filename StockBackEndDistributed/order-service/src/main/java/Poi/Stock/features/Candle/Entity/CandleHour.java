package Poi.Stock.features.Candle.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candle_hour", indexes = { @Index(name = "idx_candle_hour_stock_time", columnList = "stockCode, time") })
public class CandleHour implements Candle {
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
}