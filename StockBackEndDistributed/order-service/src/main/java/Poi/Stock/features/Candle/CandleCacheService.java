package Poi.Stock.features.Candle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandleCacheService {

	private static final int ONE_MIN_MAX_SIZE = 100;
	private static final int FIVE_MIN_MAX_SIZE = 100;
	private static final int HOUR_MAX_SIZE = 100;
	private static final int DAY_MAX_SIZE = 100;

	private static final List<Integer> MA_PERIODS = List.of(3, 5, 10, 20, 60, 120);

	private final CandleCache candleCache;

	// ==================== 1분봉 ====================
	public void putOneMinCandles(String stockCode, List<CandleMinute> candles) {
		putCandles(candleCache.getOneMinCandles(), stockCode, candles, ONE_MIN_MAX_SIZE);
	}

	// 🎯 누락되었던 복수 추가(add) 메서드 추가
	public void addOneMinCandles(String stockCode, List<CandleMinute> candles) {
		addCandles(candleCache.getOneMinCandles(), stockCode, candles, ONE_MIN_MAX_SIZE);
	}

	public void addOneMinCandle(String stockCode, CandleMinute candle) {
		addCandle(candleCache.getOneMinCandles(), stockCode, candle, ONE_MIN_MAX_SIZE);
	}

	public List<CandleWithMA<CandleMinute>> getOneMinCandles(String stockCode) {
		return getCandles(candleCache.getOneMinCandles(), stockCode);
	}

	// ==================== 5분봉 ====================
	public void putFiveMinCandles(String stockCode, List<CandleMinute> candles) {
		putCandles(candleCache.getFiveMinCandles(), stockCode, candles, FIVE_MIN_MAX_SIZE);
	}

	public void addFiveMinCandles(String stockCode, List<CandleMinute> candles) {
		addCandles(candleCache.getFiveMinCandles(), stockCode, candles, FIVE_MIN_MAX_SIZE);
	}

	public void addFiveMinCandle(String stockCode, CandleMinute candle) {
		addCandle(candleCache.getFiveMinCandles(), stockCode, candle, FIVE_MIN_MAX_SIZE);
	}

	public List<CandleWithMA<CandleMinute>> getFiveMinCandles(String stockCode) {
		return getCandles(candleCache.getFiveMinCandles(), stockCode);
	}

	// ==================== 시봉 ====================
	public void putHourCandles(String stockCode, List<CandleHour> candles) {
		putCandles(candleCache.getHourCandles(), stockCode, candles, HOUR_MAX_SIZE);
	}

	public void addHourCandles(String stockCode, List<CandleHour> candles) {
		addCandles(candleCache.getHourCandles(), stockCode, candles, HOUR_MAX_SIZE);
	}

	public void addHourCandle(String stockCode, CandleHour candle) {
		addCandle(candleCache.getHourCandles(), stockCode, candle, HOUR_MAX_SIZE);
	}

	public List<CandleWithMA<CandleHour>> getHourCandles(String stockCode) {
		return getCandles(candleCache.getHourCandles(), stockCode);
	}

	// ==================== 일봉 ====================
	public void putDayCandles(String stockCode, List<CandleDay> candles) {
		putCandles(candleCache.getDayCandles(), stockCode, candles, DAY_MAX_SIZE);
	}

	public void addDayCandles(String stockCode, List<CandleDay> candles) {
		addCandles(candleCache.getDayCandles(), stockCode, candles, DAY_MAX_SIZE);
	}

	public void addDayCandle(String stockCode, CandleDay candle) {
		addCandle(candleCache.getDayCandles(), stockCode, candle, DAY_MAX_SIZE);
	}

	public List<CandleWithMA<CandleDay>> getDayCandles(String stockCode) {
		return getCandles(candleCache.getDayCandles(), stockCode);
	}

	// ==================== 제네릭 코어 로직 ====================

	private <T extends Candle> void putCandles(Map<String, Deque<CandleWithMA<T>>> cache, String stockCode,
			List<T> candles, int maxSize) {
		Deque<CandleWithMA<T>> deque = new ArrayDeque<>();

		if (candles != null) {
			for (T candle : candles) {
				CandleWithMA<T> wrapped = calculateLiveMA(deque, candle);
				deque.addLast(wrapped);
				if (deque.size() > maxSize) {
					deque.removeFirst();
				}
			}
		}
		cache.put(stockCode, deque);
	}

	// 🎯 복수 캔들 추가를 실시간 이평선 흐름에 맞춰 연산할 제네릭 메서드
	private <T extends Candle> void addCandles(Map<String, Deque<CandleWithMA<T>>> cache, String stockCode,
			List<T> candles, int maxSize) {
		if (candles == null || candles.isEmpty())
			return;

		cache.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<T>> deque = existing == null ? new ArrayDeque<>() : existing;

			for (T candle : candles) {
				CandleWithMA<T> wrapped = calculateLiveMA(deque, candle);
				deque.addLast(wrapped);

				while (deque.size() > maxSize) {
					deque.removeFirst();
				}
			}
			return deque;
		});
	}

	private <T extends Candle> void addCandle(Map<String, Deque<CandleWithMA<T>>> cache, String stockCode, T candle,
			int maxSize) {
		if (candle == null)
			return;

		cache.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<T>> deque = existing == null ? new ArrayDeque<>() : existing;
			CandleWithMA<T> wrapped = calculateLiveMA(deque, candle);
			deque.addLast(wrapped);

			while (deque.size() > maxSize) {
				deque.removeFirst();
			}
			return deque;
		});
	}

	private <T extends Candle> CandleWithMA<T> calculateLiveMA(Deque<CandleWithMA<T>> deque, T newCandle) {
		Map<Integer, Double> maMap = new HashMap<>();

		List<Integer> prices = new ArrayList<>();
		for (CandleWithMA<T> c : deque) {
			prices.add(c.getCandle().getClose());
		}
		prices.add(newCandle.getClose());

		int totalSize = prices.size();

		for (int period : MA_PERIODS) {
			if (totalSize >= period) {
				double avg = prices.subList(totalSize - period, totalSize).stream().mapToInt(Integer::intValue)
						.average()
						.orElse(0.0);
				maMap.put(period, Math.round(avg * 100) / 100.0);
			} else {
				double avg = prices.stream().mapToInt(Integer::intValue).average().orElse(0.0);
				maMap.put(period, Math.round(avg * 100) / 100.0);
			}
		}

		return new CandleWithMA<>(newCandle, maMap);
	}

	private <T> List<CandleWithMA<T>> getCandles(Map<String, Deque<CandleWithMA<T>>> cache, String stockCode) {
		Deque<CandleWithMA<T>> candles = cache.get(stockCode);
		if (candles == null || candles.isEmpty()) {
			return List.of();
		}
		return new ArrayList<>(candles);
	}
}