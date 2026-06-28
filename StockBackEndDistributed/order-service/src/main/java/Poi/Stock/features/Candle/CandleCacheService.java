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
	private static final List<Integer> MA_PERIODS = List.of(5, 20, 60);
	private final CandleCache candleCache;

	public void putCandles(CandleType type, String stockCode, List<Candle> candles) {
		if (candles == null || candles.isEmpty())
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		Deque<CandleWithMA<Candle>> deque = new ArrayDeque<>();

		log.info("========== [putCandles 시작] 종목: {}, 타입: {}, 입력 캔들 개수: {} ==========", stockCode, type, candles.size());

		for (Candle candle : candles) {
			if (!deque.isEmpty() && deque.getLast().getCandle().getCandleTime().equals(candle.getCandleTime())) {
				deque.removeLast();
			}
			CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle);
			log.info("[캐시 적재 중] 시간: {}, 종가: {}, 계산된 이평선(MA): {}", candle.getCandleTime(), candle.getClose(),
					wrapped.getMa());
			deque.addLast(wrapped);
			if (deque.size() > MAX_CACHE_SIZE) {
				deque.removeFirst();
			}
		}

		log.info("[캐시 최종 저장] 종목: {}, 최종 데크 크기: {}", stockCode, deque.size());
		if (!deque.isEmpty()) {
			log.info("[캐시 맨 마지막 데이터 확인] 시간: {}, MA: {}", deque.getLast().getCandle().getCandleTime(),
					deque.getLast().getMa());
		}
		log.info("==========================================================================");

		cacheMap.put(stockCode, deque);
	}
	public void upsertCandles(CandleType type, String stockCode, List<Candle> newCandles) {
		if (newCandles == null || newCandles.isEmpty())
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		cacheMap.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<Candle>> deque = existing == null ? new ArrayDeque<>() : existing;

			for (Candle newCandle : newCandles) {
				if (!deque.isEmpty() && deque.getLast().getCandle().getCandleTime().equals(newCandle.getCandleTime())) {
					deque.removeLast();
				}
				CandleWithMA<Candle> wrapped = calculateLiveMA(deque, newCandle);
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
	public CandleWithMA<Candle> upsertCandle(CandleType type, String stockCode, Candle candle) {
		if (candle == null)
			return null;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);

		Deque<CandleWithMA<Candle>> finalDeque = cacheMap.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<Candle>> deque = existing == null ? new ArrayDeque<>() : existing;

			if (!deque.isEmpty() && deque.getLast().getCandle().getCandleTime().equals(candle.getCandleTime())) {
				deque.removeLast();
			}

			CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle);
			deque.addLast(wrapped);

			while (deque.size() > MAX_CACHE_SIZE) {
				deque.removeFirst();
			}
			return deque;
		});

		return finalDeque != null ? finalDeque.getLast() : null;
	}
	public void updateLastCandle(CandleType type, String stockCode, Candle candle) {
		if (candle == null)
			return;

		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		cacheMap.compute(stockCode, (key, existing) -> {
			Deque<CandleWithMA<Candle>> deque = existing == null ? new ArrayDeque<>() : existing;
			if (!deque.isEmpty()) {
				deque.removeLast();
			}
			CandleWithMA<Candle> wrapped = calculateLiveMA(deque, candle);
			deque.addLast(wrapped);
			return deque;
		});
	}

	public List<CandleWithMA<Candle>> getCacheCandles(CandleType type, String stockCode) {
		Map<String, Deque<CandleWithMA<Candle>>> cacheMap = candleCache.getTypedStore(type);
		if (cacheMap == null)
			return List.of();
		Deque<CandleWithMA<Candle>> deque = cacheMap.get(stockCode);
		if (deque == null || deque.isEmpty())
			return List.of();
		return new ArrayList<>(deque);
	}


	private CandleWithMA<Candle> calculateLiveMA(Deque<CandleWithMA<Candle>> deque, Candle newCandle) {
		Map<Integer, Double> maMap = new HashMap<>();
		List<Integer> prices = new ArrayList<>(deque.stream().map(c -> c.getCandle().getClose()).toList());

		prices.add(newCandle.getClose());
		int totalSize = prices.size();

		for (int period : MA_PERIODS) {
			double avg;
			if (totalSize >= period) {
				long sum = 0;
				for (int i = totalSize - period; i < totalSize; i++) {
					sum += prices.get(i);
				}
				avg = (double) sum / period;
			}
			else {
				long sum = 0;
				for (int price : prices) {
					sum += price;
				}
				avg = (double) sum / totalSize;
			}

			maMap.put(period, Math.round(avg * 100) / 100.0);
		}

		return new CandleWithMA<>(newCandle, maMap);
	}
}