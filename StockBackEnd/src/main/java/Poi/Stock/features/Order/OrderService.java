package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.features.Websocket.OrderBookCache;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderBookCache orderBookCache;
	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;
	private final WebSocketService webSocketService;
	/**
	 * 주문 생성 및 저장
	 */
	@Transactional
	public Order createOrder(String userId, TradeDTO tradeDTO) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		// 매수 시 자산 차감(체결시로 바꿔야할거같음?)
		if (tradeDTO.getTradeType() == tradeType.BUY) {
			int totalCost = tradeDTO.getTradePrice() * tradeDTO.getQuantity();
			if (user.getAsset() < totalCost) {
				throw new RuntimeException(String.format("자산이 부족합니다. 필요: %d원, 보유: %d원", totalCost, user.getAsset()));
			}
			// 차감 제거 - settle에서 처리
		}

		// 매도 시 보유 주식 확인
		if (tradeDTO.getTradeType() == tradeType.SELL) {
			HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, tradeDTO.getStockCode())
					.orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
			if (haveStock.getQuantity() < tradeDTO.getQuantity()) {
				throw new RuntimeException(String.format("보유 수량이 부족합니다. 보유: %d주, 매도 요청: %d주", haveStock.getQuantity(),
						tradeDTO.getQuantity()));
			}
			haveStock.setQuantity(haveStock.getQuantity() - tradeDTO.getQuantity());
			haveStockRepository.save(haveStock);
		}

		// 주문 생성
		Order order = new Order();
		order.setUserId(userId);
		order.setStockCode(tradeDTO.getStockCode());
		order.setTradeType(tradeDTO.getTradeType());
		order.setQuantity(tradeDTO.getQuantity());
		order.setRemainingQuantity(tradeDTO.getQuantity());
		order.setTradePrice(tradeDTO.getTradePrice());
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(LocalDateTime.now());
		order.setPriority(System.nanoTime());
		// 일단 주문 데이터베이스에 생성
		Order savedOrder = orderRepository.save(order);
		orderBookCache.addOrder(savedOrder);
		// 주문 했으니깐 상응하는 금액의 체결 시도
		matchOrder(savedOrder);
		webSocketService.updateWebsocketHoga(tradeDTO.getStockCode());
		return savedOrder;
	}

	/**
	 * 호가창 조회 (매도/매수)
	 */
	public Map<String, Object> getOrderBook(String stockCode) {
		// 매도 호가 (가격 낮은 순)
		List<Order> sellOrders = orderRepository.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceAscPriorityAsc(
				stockCode, tradeType.SELL, List.of(OrderStatus.PENDING, OrderStatus.PARTIAL));
		// 매수 호가 (가격 높은 순)
		List<Order> buyOrders = orderRepository.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceDescPriorityAsc(
				stockCode, tradeType.BUY, List.of(OrderStatus.PENDING, OrderStatus.PARTIAL));
		Map<String, Object> orderBook = new HashMap<>();
		orderBook.put("sellOrders", sellOrders);
		orderBook.put("buyOrders", buyOrders);
		return orderBook;
	}

	/**
	 * 주문 취소
	 */
	@Transactional
	public void cancelOrder(String userId, Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

		// 본인 주문인지 확인
		if (!order.getUserId().equals(userId)) {
			throw new RuntimeException("본인의 주문만 취소할 수 있습니다");
		}

		// 이미 체결된 주문은 취소 불가
		if (order.getStatus() == OrderStatus.COMPLETED) {
			throw new RuntimeException("이미 체결된 주문은 취소할 수 없습니다");
		}

		// 매수 주문이었다면 예약된 자산 반환
		if (order.getTradeType() == tradeType.BUY) {
			StockUser user = stockUserRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

			int refundAmount = order.getTradePrice() * order.getRemainingQuantity();
			user.setAsset(user.getAsset() + refundAmount);
			stockUserRepository.save(user);
		}

		// 주문 상태 변경
		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);
	}

	// 체결 함수
	private void matchOrder(Order newOrder) {
		OrderBook orderBook = orderBookCache.get(newOrder.getStockCode());

		List<Order> oppositeOrders = newOrder.getTradeType() == tradeType.BUY
				? orderBook.getSellOrders().stream().filter(o -> o.getTradePrice() <= newOrder.getTradePrice())
						.sorted(Comparator.comparingInt(Order::getTradePrice).thenComparingLong(Order::getPriority))
						.collect(Collectors.toList())
				: orderBook.getBuyOrders().stream().filter(o -> o.getTradePrice() >= newOrder.getTradePrice()).sorted(
						Comparator.comparingInt(Order::getTradePrice).reversed().thenComparingLong(Order::getPriority))
						.collect(Collectors.toList());

		boolean matched = false;
		int lastSellOrdersPrice = 0; // ✅ 추가

		for (Order opposite : oppositeOrders) {
			if (newOrder.getRemainingQuantity() == 0)
				break;

			int fillQty = Math.min(newOrder.getRemainingQuantity(), opposite.getRemainingQuantity());
			int SellOrdersPrice = opposite.getTradePrice();
			lastSellOrdersPrice = SellOrdersPrice; // ✅ 루프마다 갱신

			newOrder.setRemainingQuantity(newOrder.getRemainingQuantity() - fillQty);
			opposite.setRemainingQuantity(opposite.getRemainingQuantity() - fillQty);

			newOrder.setStatus(newOrder.getRemainingQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);
			opposite.setStatus(opposite.getRemainingQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);

			orderRepository.save(newOrder);
			orderRepository.save(opposite);

			if (opposite.getStatus() == OrderStatus.COMPLETED) {
				orderBookCache.removeOrder(opposite);
			}

			settle(newOrder, opposite, fillQty, SellOrdersPrice);
			matched = true;
		}
		if (newOrder.getStatus() == OrderStatus.COMPLETED) {
			orderBookCache.removeOrder(newOrder);
		}

		if (matched) {
			updateCurrentPrice(newOrder.getStockCode(), lastSellOrdersPrice);
		}
	}

	private void updateCurrentPrice(String stockCode, int lastFillPrice) {
		OrderBook orderBook = orderBookCache.get(stockCode);

		OptionalInt lowestSell = orderBook.getSellOrders().stream().filter(o -> o.getTradePrice() != null)
				.mapToInt(Order::getTradePrice).min();

		int newCurrentPrice = lowestSell.isPresent() ? lowestSell.getAsInt() : lastFillPrice;
		webSocketService.updateCurrentPrice(stockCode, newCurrentPrice);
	}

	// 자산 업데이트 함수
	private void settle(Order newOrder, Order opposite, int fillQty, int fillPrice) {
		String buyerId = newOrder.getTradeType() == tradeType.BUY ? newOrder.getUserId() : opposite.getUserId();
		String sellerId = newOrder.getTradeType() == tradeType.BUY ? opposite.getUserId() : newOrder.getUserId();
		String stockCode = newOrder.getStockCode();
		int totalAmount = fillPrice * fillQty;

		// 매수자 자산 차감
		StockUser buyer = stockUserRepository.findById(buyerId)
				.orElseThrow(() -> new RuntimeException("매수자를 찾을 수 없습니다"));
		if (buyer.getAsset() < totalAmount) {
			throw new RuntimeException("체결 시점에 자산이 부족합니다");
		}
		buyer.setAsset(buyer.getAsset() - totalAmount);
		stockUserRepository.save(buyer);

		// 매도자 자산 증가
		StockUser seller = stockUserRepository.findById(sellerId)
				.orElseThrow(() -> new RuntimeException("매도자를 찾을 수 없습니다"));
		seller.setAsset(seller.getAsset() + totalAmount);
		stockUserRepository.save(seller);

		// 매수자 보유주식 증가
		HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(buyer, stockCode).orElseGet(() -> {
			HaveStock hs = new HaveStock();
			hs.setStockUser(buyer);
			hs.setStockCode(stockCode);
			hs.setQuantity(0);
			return hs;
		});
		haveStock.setQuantity(haveStock.getQuantity() + fillQty);
		haveStockRepository.save(haveStock);
		// refund 로직 제거 - 처음부터 체결가 기준으로 차감하므로 불필요
	}

}