package Poi.Stock.features.Order;

import java.util.List;

import org.springframework.stereotype.Service;

import Poi.Stock.features.Websocket.OrderBookCache;
import Poi.Stock.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelService {

	private final OrderRepository orderRepository;
	private final OrderBookCache orderBookCache;

	// 단건 취소 (유저/봇 공통)
	public void cancelOrder(Order order) {
		OrderBook book = orderBookCache.get(order.getStockCode());
		// 메모리에서 제거
		book.removeOrder(order);
		// DB에서 제거
		orderRepository.delete(order);
		log.info("주문 취소: {} / {} / {}", order.getUserId(), order.getStockCode(), order.getTradePrice());
	}

	// 특정 유저의 특정 종목 주문 전체 취소 (봇용)
	public void cancelAllOrders(String userId, String stockCode) {
		List<Order> orders = orderRepository.findByUserIdAndStockCode(userId, stockCode);
		OrderBook book = orderBookCache.get(stockCode);
		orders.forEach(order -> book.removeOrder(order));
		orderRepository.deleteAll(orders);
		log.info("전체 주문 취소: {} / {}", userId, stockCode);
	}

	// 특정 유저의 전체 종목 주문 취소
	public void cancelAllOrders(String userId) {
		List<Order> orders = orderRepository.findByUserId(userId);
		orders.forEach(order -> {
			OrderBook book = orderBookCache.get(order.getStockCode());
			book.removeOrder(order);
		});
		orderRepository.deleteAll(orders);
		log.info("전체 주문 취소: {}", userId);
	}
}