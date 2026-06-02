package Poi.Stock.DTO.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // 💡 객체 조립 편의성을 위해 빌더 패턴 추가
@NoArgsConstructor
@AllArgsConstructor
public class CandleDTO {

	private String time;
	private int open;
	private int high;
	private int low;
	private int close;
	private Long sellQty;
	private Long buyQty;
	private Long totalVolume;
	private Long tradeAmount;

	private Map<Integer, Double> movingAverages;

	// 기본 공통 생성 팩토리 메서드
	public static CandleDTO of(String time, int open, int high, int low, int close, Long sellQty, Long buyQty,
			Long totalVolume, Long tradeAmount, Map<Integer, Double> movingAverages) {
		return new CandleDTO(time, open, high, low, close, sellQty != null ? sellQty : 0L, buyQty != null ? buyQty : 0L,
				totalVolume != null ? totalVolume : 0L, tradeAmount != null ? tradeAmount : 0L, movingAverages);
	}

	public static <T extends Candle> CandleDTO from(CandleWithMA<T> wrapped) {
		T candle = wrapped.getCandle();

		return of(getCandidateTimeStr(candle),
				candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(), candle.getBuyQty(),
				candle.getSellQty(), candle.getTotalVolume(), candle.getTradeAmount(), wrapped.getMa());
	}

	// 💡 분봉, 시봉, 일봉의 시간 필드명과 타입을 안전하게 문자열로 변환해 주는 헬퍼 메서드
	private static <T extends Candle> String getCandidateTimeStr(T candle) {
		if (candle instanceof CandleMinute) {
			return ((CandleMinute) candle).getTime().toString();
		} else if (candle instanceof Poi.Stock.features.Candle.Entity.CandleHour) {
			return ((Poi.Stock.features.Candle.Entity.CandleHour) candle).getTime().toString();
		} else if (candle instanceof CandleDay) {
			return ((CandleDay) candle).getDate().toString();
		}
		return "";
	}
	public static CandleDTO from(CandleMinute candle) {
		return of(candle.getTime().toString(), candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(),
				candle.getSellQty(), candle.getBuyQty(), candle.getTotalVolume(), candle.getTradeAmount(), Map.of());
	}

	public static CandleDTO from(CandleDay candle) {
		return of(candle.getDate().toString(), candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(),
				candle.getSellQty(), candle.getBuyQty(), candle.getTotalVolume(), candle.getTradeAmount(), Map.of());
	}

	public static CandleDTO current(LocalDateTime time, int open, int high, int low, int close, Long sellQty,
			Long buyQty) {
		return of(time.toString(), open, high, low, close, sellQty, buyQty, sellQty + buyQty, 0L, Map.of());
	}

	public static CandleDTO today(LocalDate date, int open, int high, int low, int close, Long sellQty, Long buyQty) {
		return of(date.toString(), open, high, low, close, sellQty, buyQty, sellQty + buyQty, 0L, Map.of());
	}

}