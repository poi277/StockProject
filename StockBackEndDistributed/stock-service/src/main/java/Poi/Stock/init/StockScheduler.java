package Poi.Stock.init;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import Poi.Stock.Scheduler.StockTradeStatsScheduler;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockScheduler {
    private final StockRepository stockRepository;
    private final StockCache stockCache;
	private final StockTradeStatsScheduler stockTradeStatsScheduler;

	@Value("${stock.assigned-codes:}")
	private List<String> assignedCodes;

	@PostConstruct
	public void init() {
		List<Stock> latestStocks = stockRepository.findLatestStocks();

		List<Stock> targetStocks = assignedCodes.isEmpty() ? latestStocks
				: latestStocks.stream().filter(stock -> assignedCodes.contains(stock.getStockCode())).toList();

		targetStocks.forEach(stock -> stockCache.put(stock.getStockCode(), stock));
		log.info("주식 캐시 로드 완료: {}개 종목", targetStocks.size());
		stockTradeStatsScheduler.refreshFromDb();
		log.info("거래 통계 캐시 로드 완료");
	}
}