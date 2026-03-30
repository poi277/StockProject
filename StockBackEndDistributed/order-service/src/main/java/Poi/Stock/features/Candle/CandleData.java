package Poi.Stock.features.Candle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandleData {
	private int open;
	private int high;
	private int low;
	private int close;
	private long volume;
}