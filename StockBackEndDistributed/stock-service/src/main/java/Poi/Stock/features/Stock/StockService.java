package Poi.Stock.features.Stock;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import Poi.Stock.DTO.stock.StockListResponseDto;
import Poi.Stock.Scheduler.StockTradeStatsScheduler;
import Poi.Stock.features.webSocket.WebSocketService;
import Poi.Stock.object.TradeExecution;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;
	private final StockCache stockCache;
	private final WebClient.Builder webClientBuilder;
	private final StockTradeStatsScheduler stockTradeStatsScheduler;
	private final WebSocketService webSocketService;


	public List<StockListResponseDto> getAllStocks() {
		return stockCache.values().stream()
				.map(snapshot -> new StockListResponseDto(snapshot,
						stockTradeStatsScheduler.getStats(snapshot.getStockCode())))
				.toList();
	}
	public StockRealTimeSnapshot getStock(String stockCode) {
		StockRealTimeSnapshot snapshot = stockCache.get(stockCode);
		if (snapshot != null) {
			return snapshot;
		}
		Stock stock = stockRepository.findFirstByStockCodeOrderByDateDesc(stockCode)
				.orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다: " + stockCode));
		snapshot = new StockRealTimeSnapshot(stock.getStockCode(), stock.getStockName(), 0, 0, 0, 0, 0L, 0, 0.0
		);
		stockCache.put(stockCode, snapshot);
		return snapshot;
	}

	public Stock getStockByDate(String stockCode, LocalDate date) {
		return stockRepository.findByStockCodeAndDate(stockCode, date)
				.orElseThrow(() -> new RuntimeException("해당 날짜의 데이터가 없습니다"));
	}

	public List<Stock> getStockHistory(String stockCode, LocalDate startDate, LocalDate endDate) {
		return stockRepository.findByStockCodeAndDateBetweenOrderByDateDesc(stockCode, startDate, endDate);
	}

	public List<StockRealTimeSnapshot> findByCodes(List<String> codes) {
		if (codes == null || codes.isEmpty()) {
			return List.of();
		}

		return codes.stream().map(stockCache::get).filter(stock -> stock != null).toList();
	}
	public void applyTradeExecutions(List<TradeExecution> executions) {
		if (executions == null || executions.isEmpty()) {
			return;
		}
		String stockCode = executions.get(0).getStockCode();

		StockRealTimeSnapshot snapshot = stockCache.get(stockCode);
		if (snapshot == null) {
			return;
		}

		TradeExecution lastExecution = executions.get(executions.size() - 1);
		int lastPrice = lastExecution.getPrice();

		int maxPrice = executions.stream().mapToInt(TradeExecution::getPrice).max().orElse(lastPrice);
		int minPrice = executions.stream().mapToInt(TradeExecution::getPrice).min().orElse(lastPrice);
		long addedVolume = executions.stream().mapToLong(TradeExecution::getQuantity).sum();

		snapshot.setCurrentPrice(lastPrice);
		if (maxPrice > snapshot.getHighPrice()) {
			snapshot.setHighPrice(maxPrice);
		}
		if (minPrice < snapshot.getLowPrice()) {
			snapshot.setLowPrice(minPrice);
		}

		snapshot.setTotalVolume(snapshot.getTotalVolume() + addedVolume);

		int yesterdayClose = snapshot.getYesterdayClosePrice();
		int changeAmount = lastPrice - yesterdayClose;
		double changeRate = yesterdayClose != 0 ? ((double) changeAmount / yesterdayClose) * 100.0 : 0.0;

		snapshot.setChangeAmount(changeAmount);
		snapshot.setChangeRate(changeRate);

		for (TradeExecution execution : executions) {
			webSocketService.sendExecution(execution, snapshot.getHighPrice(), snapshot.getTotalVolume());
		}

		// 5. 호가창이나 메인 화면 갱신을 위한 전체 Ticker 패킷 발행
		webSocketService.sendCurrentPrice(snapshot);
	}
}