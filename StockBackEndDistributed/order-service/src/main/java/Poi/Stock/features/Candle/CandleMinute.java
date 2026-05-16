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
	private Integer open;
	private Integer high;
	private Integer low;
	private Integer close;

	// 추가
	private Long buyQty;
	private Long sellQty;
	private Double tradeAmount;

	public static CandleMinute setCandleRedis(String stockCode, LocalDateTime candleTime, Map<Object, Object> candle) {
		try {
			return new CandleMinute(null, stockCode, candleTime, Integer.parseInt(String.valueOf(candle.get("open"))),
					Integer.parseInt(String.valueOf(candle.get("high"))),
					Integer.parseInt(String.valueOf(candle.get("low"))),
					Integer.parseInt(String.valueOf(candle.get("close"))),
					parseLong(candle.get("buyQty")),
					parseLong(candle.get("sellQty")), parseDouble(candle.get("tradeAmount")));
		} catch (Exception e) {
			throw new RuntimeException("Redis 캔들 변환 실패", e);
		}
	}

	private static long parseLong(Object val) {
		return val == null ? 0L : Long.parseLong(val.toString());
	}

	private static double parseDouble(Object val) {
		return val == null ? 0.0 : Double.parseDouble(val.toString());
	}
}