package Poi.Stock.features.Candle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;

@Component
public class CandleCache {

	// 1분봉 캐시 (stockCode → 최근 N개)
	private final Map<String, List<CandleMinute>> oneMinCandles = new ConcurrentHashMap<>();

	// 5분봉 캐시 (stockCode → 최근 N개)
	private final Map<String, List<CandleMinute>> fiveMinCandles = new ConcurrentHashMap<>();

	// 💡 추가: 시간봉(60분봉) 캐시 (stockCode → 최근 N개)
	private final Map<String, List<CandleHour>> hourCandles = new ConcurrentHashMap<>();

	// 💡 추가: 일봉 캐시 (stockCode → 최근 N개)
	private final Map<String, List<CandleDay>> dayCandles = new ConcurrentHashMap<>();

	private static final int MAX_SIZE = 20; // 최근 20개 유지

	// ==================== 1분봉 Cache ====================
	public void putOneMin(String stockCode, List<CandleMinute> candles) {
		oneMinCandles.put(stockCode, new ArrayList<>(candles));
	}

	public List<CandleMinute> getOneMin(String stockCode) {
		return oneMinCandles.getOrDefault(stockCode, List.of());
	}

	// 💡 실시간 추가용: 새로운 1분봉이 생성되었을 때 리스트 끝에 추가하고 20개 유지
	public void addOneMin(String stockCode, CandleMinute candle) {
		oneMinCandles.compute(stockCode, (code, list) -> {
			List<CandleMinute> currentList = (list == null) ? new ArrayList<>() : list;
			currentList.add(candle);
			if (currentList.size() > MAX_SIZE) {
				currentList.remove(0); // 가장 오래된 캔들 제거
			}
			return currentList;
		});
	}

	// ==================== 5분봉 Cache ====================
	public void putFiveMin(String stockCode, List<CandleMinute> candles) {
		fiveMinCandles.put(stockCode, new ArrayList<>(candles));
	}

	public List<CandleMinute> getFiveMin(String stockCode) {
		return fiveMinCandles.getOrDefault(stockCode, List.of());
	}

	// 5분봉은 변환된 CandleMinute 1개를 통째로 추가
	public void addFiveMin(String stockCode, CandleMinute candle) {
		fiveMinCandles.compute(stockCode, (code, list) -> {
			List<CandleMinute> currentList = (list == null) ? new ArrayList<>() : list;
			currentList.add(candle);
			if (currentList.size() > MAX_SIZE) {
				currentList.remove(0);
			}
			return currentList;
		});
	}


	// ==================== 💡 추가: 시간봉 Cache ====================
	public void putHour(String stockCode, List<CandleHour> candles) {
		hourCandles.put(stockCode, new ArrayList<>(candles));
	}

	public List<CandleHour> getHour(String stockCode) {
		return hourCandles.getOrDefault(stockCode, List.of());
	}

	// ==================== 💡 추가: 일봉 Cache ====================
	public void putDay(String stockCode, List<CandleDay> candles) {
		dayCandles.put(stockCode, new ArrayList<>(candles));
	}

	public List<CandleDay> getDay(String stockCode) {
		return dayCandles.getOrDefault(stockCode, List.of());
	}

	// 스케줄러가 자정이나 장마감에 일봉 저장할 때 캐시 업데이트
	public void addDay(String stockCode, CandleDay candle) {
		dayCandles.compute(stockCode, (code, list) -> {
			List<CandleDay> currentList = (list == null) ? new ArrayList<>() : list;
			currentList.add(candle);
			if (currentList.size() > MAX_SIZE) {
				currentList.remove(0);
			}
			return currentList;
		});
	}

}