package Poi.Stock.DTO.user;

import java.util.Map;
import java.util.stream.Collectors;

import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleDTO implements Candle {

	// 🎯 CandleMinute과 동일한 필드 순서
	private String stockCode;
	private String time;
	private Integer open;
	private Integer high;
	private Integer low;
	private Integer close;
	private Long buyQty;
	private Long sellQty;
	private Long totalVolume;
	private Long tradeAmount;

	private Map<String, Double> movingAverages;

	@Override
	public String getCandleTime() {
		return this.time;
	}

	@Override
	public void setCandleTime(String string) {
		this.time = string;
	}

	public static CandleDTO of(String time, int open, int high, int low, int close, Long buyQty, Long sellQty,
			Long totalVolume, Long tradeAmount, Map<Integer, Double> movingAverages) {

		Map<String, Double> stringKeyMa = Map.of();
		if (movingAverages != null && !movingAverages.isEmpty()) {
			stringKeyMa = movingAverages.entrySet().stream()
					.collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
		}
		return new CandleDTO("", time, open, high, low, close, buyQty != null ? buyQty : 0L,
				sellQty != null ? sellQty : 0L, totalVolume != null ? totalVolume : 0L,
				tradeAmount != null ? tradeAmount : 0L, stringKeyMa);
	}

	public static CandleDTO from(CandleWithMA<Candle> wrapped) {
		Candle candle = wrapped.getCandle();
		return of(candle.getCandleTime(), candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(),
				candle.getBuyQty(), candle.getSellQty(), candle.getTotalVolume(), candle.getTradeAmount(),
				wrapped.getMa());
	}

	public static CandleDTO current(String date, int open, int high, int low, int close, Long sellQty,
			Long buyQty) {
		return of(date, open, high, low, close, sellQty, buyQty, sellQty + buyQty, 0L, Map.of());
	}
}