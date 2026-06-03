package Poi.Stock.features.Candle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandleCacheService {

	private static final int MAX_CACHE_SIZE = 100;

	// 🎯 차트에 기본적으로 그려질 이동평균선 기간 설정 (5선, 20선, 60선, 120선)
	private static final List<Integer> MA_PERIODS = List.of(5, 20, 60, 120);

	private final CandleCache candleCache;

	public void putCandles(CandleType type, String stockCode, List<? extends Candle> candles) {
		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		Deque<CandleWithMA<Candle>> deque = new ArrayDeque<>();

		if (candles != null) {
			for (Candle candle : candles) {
				CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle, type);
				deque.addLast(wrapped);
				if (deque.size() > MAX_CACHE_SIZE) {
					deque.removeFirst();
				}
			}
		}
		cacheMap.put(stockCode, deque);
	}

	public void addCandles(CandleType type, String stockCode, List<? extends Candle> candles) {
		if (candles == null || candles.isEmpty())
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		cacheMap.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<Candle>> deque = existing == null ? new ArrayDeque<>() : existing;
			for (Candle candle : candles) {
				CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle, type);
				deque.addLast(wrapped);
				while (deque.size() > MAX_CACHE_SIZE) {
					deque.removeFirst();
				}
			}
			return deque;
		});
	}


	public void upsertCandle(CandleType type, String stockCode, Candle candle) {
		if (candle == null)
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		cacheMap.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<Candle>> deque = existing == null ? new ArrayDeque<>() : existing;

			if (!deque.isEmpty() && deque.getLast().getCandle().getCandleTime().equals(candle.getCandleTime())) {
				deque.removeLast();
			}
			CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle, type);
			deque.addLast(wrapped);

			while (deque.size() > MAX_CACHE_SIZE) {
				deque.removeFirst();
			}
			return deque;
		});
	}

	/**
	 * 평소 경계선 사이일 때, 큐의 맨 마지막(오른쪽) 칸만 실시간 미확정 데이터로 갱신하는 메서드
	 */
	public void updateLastCandle(CandleType type, String stockCode, Candle candle) {
		if (candle == null)
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		cacheMap.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<Candle>> deque = existing == null ? new ArrayDeque<>() : existing;
			if (!deque.isEmpty()) {
				deque.removeLast(); // 현재 미완성 상태인 마지막 칸을 제거하고
			}
			// 새로 빌드된 합산 본으로 덮어쓰기 (이평선 재계산 포함)
			CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle, type);
			deque.addLast(wrapped);
			return deque;
		});
	}


	@SuppressWarnings("unchecked")
	public <T extends Candle> List<CandleWithMA<T>> getCandles(CandleType type, String stockCode) {
		Map<String, Deque<CandleWithMA<T>>> cacheMap = candleCache.getTypedStore(type);
		if (cacheMap == null)
			return List.of();

		Deque<CandleWithMA<T>> deque = cacheMap.get(stockCode);
		if (deque == null || deque.isEmpty())
			return List.of();

		return new ArrayList<>(deque);
	}

	private CandleWithMA<Candle> calculateLiveMA(Deque<CandleWithMA<Candle>> deque, Candle newCandle, CandleType type) {
		Map<Integer, Double> maMap = new HashMap<>();
		int newPrice = newCandle.getClose();

		List<Integer> prices = new ArrayList<>();
		for (CandleWithMA<Candle> c : deque) {
			prices.add(c.getCandle().getClose());
		}
		prices.add(newPrice);
		int totalSize = prices.size();

		// 내부 상수 리스트를 참조하도록 변경
		for (int period : MA_PERIODS) {
			double avg;
			if (totalSize >= period) {
				if (!deque.isEmpty() && deque.getLast().getMa().containsKey(period)) {
					double prevAvg = deque.getLast().getMa().get(period);
					double prevSum = prevAvg * period;
					int outgoingPrice = prices.get(totalSize - period - 1);
					double currentSum = prevSum - outgoingPrice + newPrice;
					avg = currentSum / period;
				} else {
					long sum = 0;
					for (int i = totalSize - period; i < totalSize; i++) {
						sum += prices.get(i);
					}
					avg = (double) sum / period;
				}
			} else {
				if (!deque.isEmpty() && deque.getLast().getMa().containsKey(period)) {
					double prevAvg = deque.getLast().getMa().get(period);
					double prevSum = prevAvg * (totalSize - 1);
					double currentSum = prevSum + newPrice;
					avg = currentSum / totalSize;
				} else {
					avg = newPrice;
				}
			}
			maMap.put(period, Math.round(avg * 100) / 100.0);
		}
		return new CandleWithMA<>(newCandle, maMap);
	}
}