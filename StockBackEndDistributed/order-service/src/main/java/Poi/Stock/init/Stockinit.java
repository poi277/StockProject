package Poi.Stock.init;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Order.Order;
import Poi.Stock.features.Order.OrderBook;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("stockInit")
@RequiredArgsConstructor
public class Stockinit {

	private final StockRepository stockRepository;
	private final OrderBookCache orderBookCache;
	private final OrderRepository orderRepository;
	private final StockCache stockCache;
	private final CandleDayRepository candleDayRepository;

	@Value("${stock.assigned-codes:}")
	private List<String> assignedCodes;

	@PostConstruct
	public void init() {

		// 1. 마스터 DB로부터 종목 정보 조회
		List<Stock> latestStocks = stockRepository.findLatestStocks();

		// assigned-codes가 비어있으면 전체, 있으면 해당 종목만
		List<Stock> targetStocks = assignedCodes.isEmpty() ? latestStocks
				: latestStocks.stream().filter(stock -> assignedCodes.contains(stock.getStockCode())).toList();

		targetStocks.forEach(stock -> {
			String stockCode = stock.getStockCode();

			// 💡 [변경 포인트 1] 어제 자 마감 일봉 데이터에서 전일 종가(yesterdayClose) 획득
			LocalDate yesterday = LocalDate.now().minusDays(1);
			int yesterdayClose = candleDayRepository.findByStockCodeAndDate(stockCode, yesterday)
					.map(CandleDay::getClose).orElse(10000); // 데이터가 없는 신규 상장 등의 경우 초기 기본가 세팅

			// 💡 [변경 포인트 2] 봇 매매전략과 엔진이 정상 가격을 인지할 수 있도록 실시간 스냅샷 생성
			StockRealTimeSnapshot snapshot = new StockRealTimeSnapshot(stockCode, stock.getStockName(), yesterdayClose, // yesterdayClosePrice
																														// (기준가)
					yesterdayClose, // currentPrice (현재가 초기화)
					yesterdayClose, // highPrice (고가 초기화)
					yesterdayClose, // lowPrice (저가 초기화)
					0L, // totalVolume (누적 거래량)
					0, // changeAmount (등락폭)
					0.0 // changeRate (등락률)
			);

			// 💡 [변경 포인트 3] Stock 엔티티 대신 실시간 스냅샷 객체 캐싱
			stockCache.put(stockCode, snapshot);

			// 2. 기존 미체결 주문 내역(호가북) 복구 로직 유지
			List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.PARTIAL);

			List<Order> sellOrders = orderRepository
					.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceAscPriorityAsc(stockCode, tradeType.SELL,
							activeStatuses);

			List<Order> buyOrders = orderRepository
					.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceDescPriorityAsc(stockCode, tradeType.BUY,
							activeStatuses);

			OrderBook orderBook = new OrderBook();

			sellOrders.forEach(orderBook::addOrder);
			buyOrders.forEach(orderBook::addOrder);

			orderBookCache.put(stockCode, orderBook);

			log.info("호가 초기화 완료: {} (sell {}, buy {})", stockCode, sellOrders.size(), buyOrders.size());
		});

		log.info("주식 {} 개 시세 스냅샷 및 호가 캐시 로드 완료", targetStocks.size());
	}
}