package Poi.Stock.features.Candle;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import lombok.Getter;

@Component
@Getter
public class CandleCache {

	private final Map<String, Deque<CandleWithMA<CandleMinute>>> oneMinCandles = new ConcurrentHashMap<>();

	private final Map<String, Deque<CandleWithMA<CandleMinute>>> fiveMinCandles = new ConcurrentHashMap<>();

	private final Map<String, Deque<CandleWithMA<CandleHour>>> hourCandles = new ConcurrentHashMap<>();

	private final Map<String, Deque<CandleWithMA<CandleDay>>> dayCandles = new ConcurrentHashMap<>();
}