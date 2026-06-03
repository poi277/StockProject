package Poi.Stock.features.Candle;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.Getter;

@Component
@Getter
public class CandleCache {

	private final Map<CandleType, Map<String, Deque<CandleWithMA<Candle>>>> candleWithMACache = new ConcurrentHashMap<>();

	public CandleCache() {
		for (CandleType type : CandleType.values()) {
			candleWithMACache.put(type, new ConcurrentHashMap<>());
		}
	}
	@SuppressWarnings("unchecked")
	public <T extends Candle> Map<String, Deque<CandleWithMA<T>>> getTypedStore(CandleType type) {
		return (Map<String, Deque<CandleWithMA<T>>>) (Map<?, ?>) candleWithMACache.get(type);
	}
}