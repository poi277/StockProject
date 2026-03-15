package Poi.Stock.features.Order;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelService {

	private final OrderRepository orderRepository;
	private final OrderBookCache orderBookCache;

	@Transactional
	public void cancelOrder(Order order) {
		OrderBook book = orderBookCache.get(order.getStockCode());
		book.removeOrder(order);
		orderRepository.delete(order);
		log.info("주문 취소: {} / {} / {}", order.getUserId(), order.getStockCode(), order.getTradePrice());
	}

	@Transactional
	public void cancelAllOrders(String userId, String stockCode) {
		List<Order> orders = orderRepository.findByUserIdAndStockCode(userId, stockCode);
		if (orders.isEmpty())
			return;
		OrderBook book = orderBookCache.get(stockCode);
		orders.forEach(order -> book.removeOrder(order));
		orderRepository.deleteAllInBatch(orders);
		log.info("전체 주문 취소: {} / {} / {}건", userId, stockCode, orders.size());
	}

	@Transactional
	public void cancelAllOrders(String userId) {
		List<Order> orders = orderRepository.findByUserId(userId);
		if (orders.isEmpty())
			return;
		orders.forEach(order -> {
			OrderBook book = orderBookCache.get(order.getStockCode());
			book.removeOrder(order);
		});
		orderRepository.deleteAllInBatch(orders);
		log.info("전체 주문 취소: {} / {}건", userId, orders.size());
	}

	@Transactional
	public void cancelOutOfRange(String userId, String stockCode, int currentPrice, int tickSize, int hogaLevel) {
	    OrderBook book = orderBookCache.get(stockCode);
	    if (book == null) return;

	    int range = tickSize * hogaLevel;

	    // DB 조회 대신 메모리에서 직접 필터링
	    List<Order> toCancel = Stream.concat(
	            book.getSellBook().values().stream().flatMap(level -> level.getOrders().stream()),
	            book.getBuyBook().values().stream().flatMap(level -> level.getOrders().stream())
	    )
	    .filter(o -> o.getUserId().equals(userId))
	    .filter(o -> Math.abs(o.getTradePrice() - currentPrice) > range)
	    .toList();

	    if (toCancel.isEmpty()) return;
		// 메모리에서 제거
	    toCancel.forEach(book::removeOrder);
	    orderRepository.deleteAllInBatch(toCancel);
	    log.info("범위 초과 주문 취소: {} / {}건", userId, toCancel.size());
	}

}