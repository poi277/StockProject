package Poi.Stock.features.Stock;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDailyPriceId implements Serializable {
	private String stockCode;
	private LocalDate date;
}