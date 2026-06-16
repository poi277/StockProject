package Poi.Stock.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.AssignedCodeHolder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("init")
@RequiredArgsConstructor
public class Init {

	private final StockRepository stockRepository;
	private final StockCache stockCache;
	private final CandleDayRepository candleDayRepository;
	private final CandleMinuteRepository candleMinuteRepository;
	private final AssignedCodeHolder assignedCodeHolder;

	@Value("${stock.assigned-codes:}")
	private List<String> assignedCodes;

	@PostConstruct
	public void init() {

		List<Stock> latestStocks = stockRepository.findLatestStocks();

		List<Stock> targetStocks = assignedCodes.isEmpty() ? latestStocks
				: latestStocks.stream().filter(stock -> assignedCodes.contains(stock.getStockCode())).toList();

		assignedCodeHolder.setAssignedCodes(targetStocks.stream().map(Stock::getStockCode).toList());

		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

		targetStocks.forEach(stock -> {

			String stockCode = stock.getStockCode();

			int latestClose = candleDayRepository.findTopByStockCodeOrderByDateDesc(stockCode).map(CandleDay::getClose)
					.orElse(10000);

			List<CandleMinute> todayCandles = candleMinuteRepository
					.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, startOfDay, endOfDay);

			int currentPrice, highPrice, lowPrice;
			long totalVolume;

			if (todayCandles.isEmpty()) {
				currentPrice = highPrice = lowPrice = latestClose;
				totalVolume = 0L;
			} else {
				currentPrice = todayCandles.get(todayCandles.size() - 1).getClose();
				highPrice = todayCandles.stream().mapToInt(CandleMinute::getHigh).max().orElse(latestClose);
				lowPrice = todayCandles.stream().mapToInt(CandleMinute::getLow).min().orElse(latestClose);
				totalVolume = todayCandles.stream().mapToLong(CandleMinute::getTotalVolume).sum();
			}

			int changeAmount = currentPrice - latestClose;
			double changeRate = latestClose == 0 ? 0.0 : (changeAmount / (double) latestClose) * 100;

			StockRealTimeSnapshot snapshot = new StockRealTimeSnapshot(stockCode, stock.getStockName(), latestClose,
					currentPrice, highPrice, lowPrice, totalVolume, changeAmount, changeRate);

			stockCache.put(stockCode, snapshot);

			log.info("시세 캐시 초기화 완료: {}", stockCode);
		});

		log.info("주식 {}개 시세 캐시 초기화 완료", targetStocks.size());
	}
}