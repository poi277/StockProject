package Poi.Stock.features.Order;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import Poi.Stock.features.CompletedOrder.CompletedOrder;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelService {

	private final OrderRepository orderRepository;
	private final OrderBookCache orderBookCache;
	@Value("${user.service.url}")
	private String userServiceUrl;
	private final WebSocketService webSocketService;
	private final CompletedOrderRepository completedOrderRepository;
	private final RestTemplate restTemplate;

	@Transactional
	public void cancelBotOrder(Order order) {
		OrderBook book = orderBookCache.get(order.getStockCode());
		book.removeOrder(order);
		orderRepository.delete(order);
		log.info("주문 취소: {} / {} / {}", order.getUserId(), order.getStockCode(), order.getTradePrice());
	}

	@Transactional
	public void cancelOrder(String userId, Long orderId, String accessToken) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));
		if (!order.getUserId().equals(userId)) {
			throw new RuntimeException("본인의 주문만 취소할 수 있습니다");
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		restTemplate.exchange(userServiceUrl + "/user/cancel-reserve", HttpMethod.POST,
				new HttpEntity<>(Map.of("tradeType", order.getTradeType().name(), "stockCode", order.getStockCode(),
						"price", order.getTradePrice(), "quantity", order.getRemainingQuantity()), headers),
				Void.class);

		OrderBook book = orderBookCache.get(order.getStockCode());
		book.removeOrder(order);

		// 취소된 가격의 남은 수량 계산 후 웹소켓 전송
		PriceLevel level = order.getTradeType() == tradeType.BUY ? book.getBuyBook().get(order.getTradePrice())
				: book.getSellBook().get(order.getTradePrice());
		int remainingQty = level == null ? 0 : level.getTotalQuantity();
		webSocketService.sendHoga(order.getStockCode(), order.getTradeType(), order.getTradePrice(), remainingQty);

		CompletedOrder completedOrder = CompletedOrder.fromCancelledOrder(order);
		completedOrderRepository.save(completedOrder);
		orderRepository.delete(order);
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