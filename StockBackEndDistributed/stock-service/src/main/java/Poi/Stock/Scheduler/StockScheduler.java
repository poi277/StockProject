package Poi.Stock.Scheduler;

import java.util.List;

import org.springframework.stereotype.Component;

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

    // 서버 시작 시 최신 주식 데이터 캐시 로드
	@PostConstruct
	public void init() {
		List<Stock> latestStocks = stockRepository.findLatestStocks();
		latestStocks.forEach(stock -> stockCache.put(stock.getStockCode(), stock));
		log.info("주식 캐시 로드 완료: {}개 종목", latestStocks.size());

		stockTradeStatsScheduler.refreshFromDb(); // 추가
		log.info("거래 통계 캐시 로드 완료");
	}
}
