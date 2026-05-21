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
				.map(stock -> new StockListResponseDto(stock, stockTradeStatsScheduler.getStats(stock.getStockCode())))
				.toList();
    }

    public Stock getStock(String stockCode) {
        Stock stock = stockCache.get(stockCode);
        if (stock != null) return stock;
        stock = stockRepository.findFirstByStockCodeOrderByDateDesc(stockCode)
                .orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다: " + stockCode));
        stockCache.put(stockCode, stock);
        return stock;
    }

    public Stock getStockByDate(String stockCode, LocalDate date) {
        return stockRepository.findByStockCodeAndDate(stockCode, date)
                .orElseThrow(() -> new RuntimeException("해당 날짜의 데이터가 없습니다"));
    }

    public List<Stock> getStockHistory(String stockCode, LocalDate startDate, LocalDate endDate) {
        return stockRepository.findByStockCodeAndDateBetweenOrderByDateDesc(stockCode, startDate, endDate);
    }


    public void updateCurrentPrice(String stockCode, int lastFillPrice) {
        Stock stock = stockCache.get(stockCode);
        if (stock == null || stock.getClosePrice() == lastFillPrice) return;
        stock.setClosePrice(lastFillPrice);
        stock.setHighPrice(Math.max(stock.getHighPrice(), lastFillPrice));
        stock.setLowPrice(Math.min(stock.getLowPrice(), lastFillPrice));
        int changeAmount = lastFillPrice - stock.getOpenPrice();
        stock.setChangeAmount(changeAmount);
        stock.setChangeRate((double) changeAmount / stock.getOpenPrice() * 100);
        stockCache.put(stockCode, stock);
    }

	public List<Stock> findByCodes(List<String> codes) {
		return stockRepository.findByStockCodeIn(codes);
	}

	public void applyTradeExecutions(List<TradeExecution> executions) {
		String stockCode = executions.get(0).getStockCode();
		Stock stock = stockCache.get(stockCode);
		if (stock == null)
			return;

		for (TradeExecution execution : executions) {
			stock.setClosePrice(execution.getPrice());
			if (stock.getHighPrice() == null || execution.getPrice() > stock.getHighPrice())
				stock.setHighPrice(execution.getPrice());
			if (stock.getLowPrice() == null || execution.getPrice() < stock.getLowPrice())
				stock.setLowPrice(execution.getPrice());
			if (stock.getOpenPrice() != null) {
				stock.setChangeAmount(execution.getPrice() - stock.getOpenPrice());
				stock.setChangeRate(stock.calcChangeRate(execution.getPrice()));
			}
			stock.setTotalvolume(stock.getTotalvolume() + execution.getQuantity());
			webSocketService.sendExecution(execution, stock);
		}

		stockCache.put(stockCode, stock);
		stockRepository.save(stock);
		webSocketService.SendCurrentPrice(stock);
	}

}
