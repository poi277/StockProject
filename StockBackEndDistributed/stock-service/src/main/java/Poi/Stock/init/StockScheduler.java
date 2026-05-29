package Poi.Stock.init;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import Poi.Stock.Scheduler.StockTradeStatsScheduler;
import Poi.Stock.features.Candle.CandleDay;
import Poi.Stock.features.Candle.CandleDayRepository;
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

	@Value("${stock.assigned-codes:}")
	private List<String> assignedCodes;

	@PostConstruct
	public void init() {
		// 1. 마스터 DB로부터 종목 기본 정보 리스트 업
		List<Stock> latestStocks = stockRepository.findLatestStocks();

		List<Stock> targetStocks = assignedCodes.isEmpty() ? latestStocks
				: latestStocks.stream().filter(stock -> assignedCodes.contains(stock.getStockCode())).toList();

		// 2. 각 종목별로 전일 종가를 찾아 실시간 인메모리 스냅샷 조립 및 캐싱
		targetStocks.forEach(stock -> {
			String stockCode = stock.getStockCode();

			// 어제 날짜 기준으로 정산 완료된 일봉(CandleDay) 데이터를 조회하여 기준 종가를 획득합니다.
			LocalDate yesterday = LocalDate.now().minusDays(1);
			int yesterdayClose = candleDayRepository.findByStockCodeAndDate(stockCode, yesterday)
					.map(CandleDay::getClose).orElse(10000); // 만약 첫 상장 주식이거나 어제 데이터가 없다면 임시 기준가(ex: 10,000원) 제공 혹은
																// 예외 처리

			// RDB 엔티티 대신 순수 메모리 연산용 스냅샷 객체 생성 (시가, 고가, 저가, 현재가를 모두 전일종가로 맞추어 장 시작 대기)
			StockRealTimeSnapshot snapshot = new StockRealTimeSnapshot(stockCode, stock.getStockName(), yesterdayClose, // yesterdayClosePrice
																														// (기준점)
					yesterdayClose, // currentPrice (현재가)
					yesterdayClose, // highPrice (당일 고가)
					yesterdayClose, // lowPrice (당일 저가)
					0L, // totalVolume (당일 거래량 초기화)
					0, // changeAmount (당일 등락폭 초기화)
					0.0 // changeRate (당일 등락률 초기화)
			);

			// 변경된 인메모리 캐시에 세팅
			stockCache.put(stockCode, snapshot);
		});

		log.info("주식 인메모리 시세 캐시 및 전일 종가 로드 완료: {}개 종목", targetStocks.size());

		stockTradeStatsScheduler.refreshFromDb();
		log.info("거래 통계 캐시 로드 완료");
	}
}