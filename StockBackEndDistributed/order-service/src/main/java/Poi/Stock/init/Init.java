package Poi.Stock.init;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
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
	private final AssignedCodeHolder assignedCodeHolder;

	@Value("${stock.assigned-codes:}")
	private List<String> assignedCodes;

	@PostConstruct
	public void init() {

		List<Stock> latestStocks = stockRepository.findLatestStocks();

		List<Stock> targetStocks = assignedCodes.isEmpty() ? latestStocks
				: latestStocks.stream().filter(stock -> assignedCodes.contains(stock.getStockCode())).toList();

		assignedCodeHolder.setAssignedCodes(targetStocks.stream().map(Stock::getStockCode).toList());

		targetStocks.forEach(stock -> {

			String stockCode = stock.getStockCode();

			LocalDate yesterday = LocalDate.now().minusDays(1);

			int yesterdayClose = candleDayRepository.findByStockCodeAndDate(stockCode, yesterday)
					.map(CandleDay::getClose).orElse(10000);

			StockRealTimeSnapshot snapshot = new StockRealTimeSnapshot(stockCode, stock.getStockName(), yesterdayClose,
					yesterdayClose, yesterdayClose, yesterdayClose, 0L, 0, 0.0);

			stockCache.put(stockCode, snapshot);

			log.info("시세 캐시 초기화 완료: {}", stockCode);
		});

		log.info("주식 {}개 시세 캐시 초기화 완료", targetStocks.size());
	}
}