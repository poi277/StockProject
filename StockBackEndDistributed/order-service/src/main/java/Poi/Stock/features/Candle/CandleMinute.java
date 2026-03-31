package Poi.Stock.features.Candle;

import java.time.LocalDateTime;
import java.util.Map;

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
@Table(name = "candle_minute", indexes = { @Index(name = "idx_candle_stock_time", columnList = "stockCode, time") })
public class CandleMinute {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String stockCode;
	private LocalDateTime time;
	// 4종은 다 있어야함
	// 시작
	private Integer open;
	private Integer high;
	private Integer low;
	// 끝
	private Integer close;
	// 거래량
	private Long volume;

	public static CandleMinute setCandleRedis(String stockCode, LocalDateTime candleTime, Map<Object, Object> current) {
		try {
			return new CandleMinute(null, stockCode, candleTime, Integer.parseInt(String.valueOf(current.get("open"))),
					Integer.parseInt(String.valueOf(current.get("high"))),
					Integer.parseInt(String.valueOf(current.get("low"))),
					Integer.parseInt(String.valueOf(current.get("close"))),
					Long.parseLong(String.valueOf(current.get("volume"))));
		} catch (Exception e) {
			throw new RuntimeException("Redis 캔들 변환 실패", e);
		}
	}
}