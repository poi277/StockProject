package Poi.Stock.init;

import java.util.List;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Order.Order;
import Poi.Stock.features.Order.OrderBook;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("orderInit")
@DependsOn("init")
@RequiredArgsConstructor
public class OrderInit {

	private final OrderBookCache orderBookCache;
	private final OrderRepository orderRepository;
	private final AssignedCodeHolder assignedCodeHolder;

	@PostConstruct
	public void init() {

		List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.PARTIAL);

		for (String stockCode : assignedCodeHolder.getAssignedCodes()) {

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
		}
	}
}