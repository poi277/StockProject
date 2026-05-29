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

	/**
	 * 실시간 인메모리 스냅샷 캐시로부터 전 종목의 현재 시세 데이터를 리스트로 가져옵니다.
	 */
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

		// Fallback 스냅샷 생성 및 적재 (이전 가이드라인에 맞춰 생성자 호출)
		snapshot = new StockRealTimeSnapshot(stock.getStockCode(), stock.getStockName(), 0, 0, 0, 0, 0L, 0, 0.0 // 기본 정산
																												// 수치들
																												// 초기화
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

		// DB 엔티티가 아닌 고속 인메모리 실시간 스냅샷을 가져옴
		StockRealTimeSnapshot snapshot = stockCache.get(stockCode);
		if (snapshot == null) {
			return;
		}

		TradeExecution lastExecution = executions.get(executions.size() - 1);
		int lastPrice = lastExecution.getPrice();

		int maxPrice = executions.stream().mapToInt(TradeExecution::getPrice).max().orElse(lastPrice);
		int minPrice = executions.stream().mapToInt(TradeExecution::getPrice).min().orElse(lastPrice);
		long addedVolume = executions.stream().mapToLong(TradeExecution::getQuantity).sum();

		// 1. 실시간 현재가(종가 대체), 고가, 저가 메모리 업데이트
		snapshot.setCurrentPrice(lastPrice);
		if (maxPrice > snapshot.getHighPrice()) {
			snapshot.setHighPrice(maxPrice);
		}
		if (minPrice < snapshot.getLowPrice()) {
			snapshot.setLowPrice(minPrice);
		}

		// 2. 누적 거래량 업데이트
		snapshot.setTotalVolume(snapshot.getTotalVolume() + addedVolume);

		// 3. 도메인 규칙: 변하지 않는 전일 종가(Yesterday Close) 기반으로 실시간 등락폭 & 등락률 계산
		int yesterdayClose = snapshot.getYesterdayClosePrice();
		int changeAmount = lastPrice - yesterdayClose;
		double changeRate = yesterdayClose != 0 ? ((double) changeAmount / yesterdayClose) * 100.0 : 0.0;

		snapshot.setChangeAmount(changeAmount);
		snapshot.setChangeRate(changeRate);

		// 4. 개별 체결 데이터에 실시간 지표를 매핑하여 웹소켓 브로드캐스팅
		for (TradeExecution execution : executions) {
			webSocketService.sendExecution(execution, snapshot.getHighPrice(), snapshot.getTotalVolume());
		}

		// 5. 호가창이나 메인 화면 갱신을 위한 전체 Ticker 패킷 발행
		webSocketService.sendCurrentPrice(snapshot);
	}
}