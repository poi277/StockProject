package Poi.Stock.features.Candle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleCacheService {

	private static final int MAX_CACHE_SIZE = 100;

	// 🎯 차트에 기본적으로 그려질 이동평균선 기간 설정 (5선, 20선, 60선, 120선)
	private static final List<Integer> MA_PERIODS = List.of(5, 20, 60);

	private final CandleCache candleCache;

	public void putCandles(CandleType type, String stockCode, List<? extends Candle> candles) {
		if (candles == null || candles.isEmpty())
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		Deque<CandleWithMA<Candle>> deque = new ArrayDeque<>();

		for (Candle candle : candles) {
			if (!deque.isEmpty() && deque.getLast().getCandle().getCandleTime().equals(candle.getCandleTime())) {
				deque.removeLast();
			}

			CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle, type);
			deque.addLast(wrapped);

			if (deque.size() > MAX_CACHE_SIZE) {
				deque.removeFirst();
			}
		}
		cacheMap.put(stockCode, deque);
	}

	public void upsertCandles(CandleType type, String stockCode, List<? extends Candle> candles) {
		if (candles == null || candles.isEmpty())
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		cacheMap.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<Candle>> deque = existing == null ? new ArrayDeque<>() : existing;

			for (Candle candle : candles) {

				if (!deque.isEmpty()) {
					String lastTime = deque.getLast().getCandle().getCandleTime();
					if (lastTime.equals(candle.getCandleTime())) {
						deque.removeLast();
					}
				}
				CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle, type);
				deque.addLast(wrapped);
				while (deque.size() > MAX_CACHE_SIZE) {
					deque.removeFirst();
				}
			}

			if (log.isInfoEnabled()) {
				String queueDetails = deque.stream()
						.map(c -> String.format("[%s -> MA:%s]", c.getCandle().getCandleTime(), c.getMa().toString()))
						.collect(Collectors.joining(", "));
				log.info("[{}] {}분봉 큐 전체 리스트 확인 (사이즈: {}) \n 👉 {}", stockCode, type.getMinute(), deque.size(),
						queueDetails);
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

			if (log.isInfoEnabled()) {
				String queueDetails = deque.stream()
						.map(c -> String.format("[%s -> MA:%s]", c.getCandle().getCandleTime(), c.getMa().toString()))
						.collect(Collectors.joining(", "));
				log.info("[{}] {}분봉 큐 전체 리스트 확인 (사이즈: {}) \n 👉 {}", stockCode, type.getMinute(), deque.size(),
						queueDetails);
			}

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
		List<Integer> prices = new ArrayList<>(deque.stream().map(c -> c.getCandle().getClose()).toList());

		int newPrice = newCandle.getClose();
		prices.add(newPrice);

		int totalSize = prices.size();
		// 2. 이평선 기간 순회 (5, 20, 60, 120)
		for (int period : MA_PERIODS) {
			double avg;

			// 데이터가 충분히 쌓였을 때 (정석 이평선 계산)
			if (totalSize >= period) {
				long sum = 0;
				// 딱 필요한 기간(최신 N개)만큼만 깔끔하게 합산
				for (int i = totalSize - period; i < totalSize; i++) {
					sum += prices.get(i);
				}
				avg = (double) sum / period;
			}
			// 데이터가 아직 부족할 때 (현재까지 쌓인 개수만큼만 누적 평균)
			else {
				long sum = 0;
				for (int price : prices) {
					sum += price;
				}
				avg = (double) sum / totalSize;
			}

			// 소수점 둘째 자리 반올림 후 저장
			maMap.put(period, Math.round(avg * 100) / 100.0);
		}

		return new CandleWithMA<>(newCandle, maMap);
	}
}