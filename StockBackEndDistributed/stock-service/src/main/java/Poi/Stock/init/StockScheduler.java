package Poi.Stock.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import Poi.Stock.Scheduler.StockTradeStatsScheduler;
import Poi.Stock.features.Candle.CandleDay;
import Poi.Stock.features.Candle.CandleDayRepository;
import Poi.Stock.features.Candle.CandleMinute;
import Poi.Stock.features.Candle.CandleMinuteRepository;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockScheduler {

	private final StockRepository stockRepository;
	private final CandleDayRepository candleDayRepository;
	private final StockCache stockCache;
	private final StockTradeStatsScheduler stockTradeStatsScheduler;
	private final CandleMinuteRepository candleMinuteRepository;

	@Value("${stock.assigned-codes:}")
	private List<String> assignedCodes;

	@PostConstruct
	public void init() {
		// 1. 마스터 DB로부터 종목 기본 정보 리스트 업
		List<Stock> latestStocks = stockRepository.findLatestStocks();

		List<Stock> targetStocks = assignedCodes.isEmpty() ? latestStocks
				: latestStocks.stream().filter(stock -> assignedCodes.contains(stock.getStockCode())).toList();

		LocalDate today = LocalDate.now();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

		// 2. 각 종목별로 전일 종가 + 당일 진행 데이터를 복구하여 실시간 인메모리 스냅샷 조립 및 캐싱
		targetStocks.forEach(stock -> {
			String stockCode = stock.getStockCode();

			// 가장 최근 일봉 데이터를 조회하여 기준 종가를 획득
			int yesterdayClose = candleDayRepository.findTopByStockCodeOrderByDateDesc(stockCode)
					.map(CandleDay::getClose).orElse(10000);

			// 당일 분봉을 조회하여 진행 중이던 현재가/고가/저가/거래량 복구
			List<CandleMinute> todayCandles = candleMinuteRepository
					.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, startOfDay, endOfDay);

			int currentPrice, highPrice, lowPrice;
			long totalVolume;

			if (todayCandles.isEmpty()) {
				currentPrice = highPrice = lowPrice = yesterdayClose;
				totalVolume = 0L;
			} else {
				currentPrice = todayCandles.get(todayCandles.size() - 1).getClose();
				highPrice = todayCandles.stream().mapToInt(CandleMinute::getHigh).max().orElse(yesterdayClose);
				lowPrice = todayCandles.stream().mapToInt(CandleMinute::getLow).min().orElse(yesterdayClose);
				totalVolume = todayCandles.stream().mapToLong(CandleMinute::getTotalVolume).sum();
			}

			int changeAmount = currentPrice - yesterdayClose;
			double changeRate = yesterdayClose == 0 ? 0.0 : (changeAmount / (double) yesterdayClose) * 100;

			StockRealTimeSnapshot snapshot = new StockRealTimeSnapshot(stockCode, stock.getStockName(), yesterdayClose,
					currentPrice, highPrice, lowPrice, totalVolume, changeAmount, changeRate);

			stockCache.put(stockCode, snapshot);
		});

		log.info("주식 인메모리 시세 캐시 및 당일 진행 데이터 복구 완료: {}개 종목", targetStocks.size());

		stockTradeStatsScheduler.refreshFromDb();
		log.info("거래 통계 캐시 로드 완료");
	}
}