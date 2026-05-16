package Poi.Stock.features.Stock;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "candle_minute")
public class CandleMinute {
	@Id
	private Long id;
	private String stockCode;
	private LocalDateTime time;
	private Long buyQty;
	private Long sellQty;
	private Double tradeAmount;
}