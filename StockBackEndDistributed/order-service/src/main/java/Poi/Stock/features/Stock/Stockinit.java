package Poi.Stock.features.Stock;

import java.util.List;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Order.Order;
import Poi.Stock.features.Order.OrderBook;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Stockinit {

	private final StockRepository stockRepository;
	private final StockCache stockCache;
	private final OrderBookCache orderBookCache;
	private final OrderRepository orderRepository;

	// 서버 시작시 DB에서 각 종목의 최신 데이터만 로드
	@PostConstruct
	public void init() {

		List<Stock> latestStocks = stockRepository.findLatestStocks();

		latestStocks.forEach(stock -> {
			String stockCode = stock.getStockCode();
			stockCache.put(stockCode, stock);
			List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.PARTIAL);
			List<Order> sellOrders = orderRepository
					.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceAscPriorityAsc(stockCode, tradeType.SELL,
							activeStatuses);
			List<Order> buyOrders = orderRepository
					.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceDescPriorityAsc(stockCode, tradeType.BUY,
							activeStatuses);
			OrderBook orderBook = new OrderBook();
			for (Order order : sellOrders) {
				orderBook.addOrder(order);
			}
			for (Order order : buyOrders) {
				orderBook.addOrder(order);
			}
			orderBookCache.put(stockCode, orderBook);

			log.info("호가 초기화 완료: {} (sell {}, buy {})", stockCode, sellOrders.size(), buyOrders.size());
		});

		log.info("주식 {} 개 및 호가 캐시 로드 완료", latestStocks.size());
	}
}
