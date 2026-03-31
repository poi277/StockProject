package Poi.Stock.features.Candle;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.repository.CandleMinuteRepository;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {

	private final CandleMinuteRepository candleMinuteRepository;
	private final StockRepository stockRepository;

	public List<Map<String, Object>> getCandle1m(String stockCode) {
		LocalDateTime from = LocalDateTime.now().minusDays(1);
		List<CandleMinute> candles = candleMinuteRepository.findByStockCodeAndTimeAfterOrderByTimeAsc(stockCode, from);
		return candles.stream().map(c -> {
			Map<String, Object> map = new HashMap<>();
			map.put("time", c.getTime().toString());
			map.put("open", c.getOpen());
			map.put("high", c.getHigh());
			map.put("low", c.getLow());
			map.put("close", c.getClose());
			map.put("volume", c.getVolume());
			return map;
		}).collect(Collectors.toList());
	}
	// 5분봉 조회 (시간 기준)
	public List<Map<String, Object>> getCandle5m(String stockCode) {
		LocalDateTime from = LocalDateTime.now().minusDays(7);
		List<CandleMinute> candles = candleMinuteRepository.findByStockCodeAndTimeAfterOrderByTimeAsc(stockCode, from);
		Map<String, List<CandleMinute>> grouped = candles.stream().collect(Collectors.groupingBy(c -> {
			LocalDateTime t = c.getTime();
			int minute = (t.getMinute() / 5) * 5;
			return t.withMinute(minute).withSecond(0).withNano(0).toString();
		}));
		return grouped.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
			List<CandleMinute> group = entry.getValue();
			group.sort(Comparator.comparing(CandleMinute::getTime));
			Map<String, Object> map = new HashMap<>();
			map.put("time", group.get(0).getTime().toString());
			map.put("open", group.get(0).getOpen());
			map.put("high", group.stream().mapToInt(CandleMinute::getHigh).max().orElse(0));
			map.put("low", group.stream().mapToInt(CandleMinute::getLow).min().orElse(0));
			map.put("close", group.get(group.size() - 1).getClose());
			map.put("volume", group.stream().mapToLong(CandleMinute::getVolume).sum());
			return map;
		}).collect(Collectors.toList());
	}
	// 일봉 조회
	public List<Map<String, Object>> getCandleDay(String stockCode) {
		List<Stock> stocks = stockRepository.findByStockCodeOrderByDateAsc(stockCode);
		return stocks.stream().map(s -> {
			Map<String, Object> map = new HashMap<>();
			map.put("time", s.getDate().toString());
			map.put("open", s.getOpenPrice());
			map.put("high", s.getHighPrice());
			map.put("low", s.getLowPrice());
			map.put("close", s.getClosePrice());
			map.put("volume", s.getTotalvolume());
			return map;
		}).collect(Collectors.toList());
	}
}