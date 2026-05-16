package Poi.Stock.Scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import Poi.Stock.features.Stock.CandleMinute;
import Poi.Stock.features.Stock.StockTradeStatus;
import Poi.Stock.repository.CandleMinuteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTradeStatsScheduler {

	private final CandleMinuteRepository candleMinuteRepository;

	// stockCode → TradeStats
	private final Map<String, StockTradeStatus> stockTradeStatusCache = new ConcurrentHashMap<>();

	@Scheduled(fixedDelay = 60_000)
	public void refreshFromDb() {
		try {
			LocalDateTime from = LocalDateTime.now().minusMinutes(30);
			List<String> stockCodes = candleMinuteRepository.findDistinctStockCodes();

			for (String stockCode : stockCodes) {
				try {
					List<CandleMinute> recent = candleMinuteRepository.findByStockCodeAndTimeAfter(stockCode, from);

					long buyQty = recent.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0).sum();
					long sellQty = recent.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0).sum();
					double tradeAmount = recent.stream()
							.mapToDouble(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0).sum();

					stockTradeStatusCache.put(stockCode, new StockTradeStatus(buyQty, sellQty, tradeAmount));
					log.info("캐시 갱신 - {} buyQty:{} sellQty:{} amount:{}", stockCode, buyQty, sellQty, tradeAmount);
				} catch (Exception e) {
					log.error("캐시 갱신 실패 - stockCode: {} error: {}", stockCode, e.getMessage());
				}
			}
		} catch (Exception e) {
			log.error("TradeStats 캐시 전체 갱신 실패: {}", e.getMessage());
		}
	}

	public StockTradeStatus getStats(String stockCode) {
		return stockTradeStatusCache.getOrDefault(stockCode, new StockTradeStatus(0, 0, 0));
	}
}