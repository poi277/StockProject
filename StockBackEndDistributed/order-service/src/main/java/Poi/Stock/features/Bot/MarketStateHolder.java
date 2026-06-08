package Poi.Stock.features.Bot;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.util.EnumUtil.CandleType;
import Poi.Stock.util.EnumUtil.MarketState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketStateHolder {

	private final Random random = new Random();
	private final CandleCacheService candleCacheService;

	private final Map<String, MarketState> stateMap = new ConcurrentHashMap<>();
	private final Map<String, Integer> intensityMap = new ConcurrentHashMap<>();

	// 1분마다 모든 종목 상태 업데이트
	// @Scheduled(fixedDelay = 60000)
	public void updateAllStocks() {
		stateMap.keySet().forEach(this::updateMarketState);
	}

	public void updateMarketState(String stockCode) {
		List<CandleWithMA<CandleMinute>> candles = candleCacheService.getCandles(CandleType.ONE_MINUTE, stockCode);
		if (candles == null || candles.isEmpty())
			return;

		Map<Integer, Double> ma = candles.get(candles.size() - 1).getMa();
		double ma5 = ma.getOrDefault(5, 0.0);
		double ma20 = ma.getOrDefault(20, 0.0);
		double ma60 = ma.getOrDefault(60, 0.0);

		if (ma5 == 0.0 || ma20 == 0.0 || ma60 == 0.0)
			return;

		// 시장 상태 결정
		MarketState state;
		if (ma5 > ma20 && ma20 > ma60) {
			state = MarketState.BULL;
		} else if (ma5 < ma20 && ma20 < ma60) {
			state = MarketState.BEAR;
		} else {
			state = MarketState.FLAT;
		}

		// 괴리율로 intensity 결정
		double divergence = Math.abs((ma5 - ma60) / ma60 * 100);
		int intensity;
		if (divergence >= 3.0) {
			intensity = 90;
		} else if (divergence >= 2.0) {
			intensity = 75;
		} else if (divergence >= 1.0) {
			intensity = 60;
		} else if (divergence >= 0.5) {
			intensity = 45;
		} else {
			intensity = 30;
		}

		stateMap.put(stockCode, state);
		intensityMap.put(stockCode, intensity);

		log.info("시장 상태 업데이트 - stockCode: {}, state: {}, intensity: {}, MA5: {}, MA20: {}, MA60: {}", stockCode, state,
				intensity, ma5, ma20, ma60);
	}

	public MarketState getState(String stockCode) {
		return stateMap.getOrDefault(stockCode, MarketState.FLAT);
	}

	public int getIntensity(String stockCode) {
		return intensityMap.getOrDefault(stockCode, 50);
	}

	public void unregister(String stockCode) {
		stateMap.remove(stockCode);
		intensityMap.remove(stockCode);
	}

	public int peoplevix(String stockCode) {
		int intensity = getIntensity(stockCode);
		int roll = random.nextInt(100);
		if (roll < intensity) {
			return random.nextInt(2) + 1;
		} else {
			return random.nextInt(2) + 2;
		}
	}
}
