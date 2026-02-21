package Poi.Stock.features.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Stock.StockService;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.features.Websocket.OrderBookCache;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockRepository;
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
	private final StockRepository stockRepository;
	private final OrderBookCache orderBookCache;
	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;
	private final StockService stockService;
	private final WebSocketService webSocketService;
	private final CompletedOrderRepository completedOrderRepository;
	private final OrderTradeService orderTradeService;

	/**
	 * 자산/보유주식 검증
	 */
	public void validateOrder(String userId, TradeDTO tradeDTO) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		if (tradeDTO.getTradeType() == tradeType.BUY) {
			int totalCost = tradeDTO.getTradePrice() * tradeDTO.getQuantity();
			if (user.getAsset() < totalCost) {
				throw new RuntimeException(String.format("자산이 부족합니다. 필요: %d원, 보유: %d원", totalCost, user.getAsset()));
			}
		}

		if (tradeDTO.getTradeType() == tradeType.SELL) {
			HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, tradeDTO.getStockCode())
					.orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
			if (haveStock.getQuantity() < tradeDTO.getQuantity()) {
				throw new RuntimeException(String.format("보유 수량이 부족합니다. 보유: %d주, 매도 요청: %d주", haveStock.getQuantity(),
						tradeDTO.getQuantity()));
			}
		}
	}

	/**
	 * 주문 접수 (검증 → 생성 → 매칭 → 정산 → 현재가 업데이트)
	 */
	@Transactional
	public Order placeOrder(String userId, TradeDTO tradeDTO) {
		// 주문 생성 및 저장
		Order order = orderTradeService.buildOrder(userId, tradeDTO);
		orderRepository.save(order);
		orderBookCache.addOrder(order);

		// 매칭 가능한 반대 주문 조회
		List<Order> matchOrderList = orderTradeService.findMatchOrderList(order);

		// 체결 처리
		int lastFillPrice = orderTradeService.processMatching(order, matchOrderList);

		orderTradeService.saveOrComplete(order);
		// 체결이 발생했으면 현재가 업데이트
		if (lastFillPrice > 0) {
			stockService.updateCurrentPrice(order.getStockCode(), lastFillPrice); // 캐시 갱신
			webSocketService.SendCurrentPrice(order.getStockCode(), lastFillPrice); // 프론트 전송
		}

		webSocketService.updateWebsocketHoga(order.getStockCode());
		return order;
	}

	public Map<String, Object> getOrderBook(String stockCode) {
		List<Order> sellOrders = orderRepository.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceAscPriorityAsc(
				stockCode, tradeType.SELL, List.of(OrderStatus.PENDING, OrderStatus.PARTIAL));
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

		if (!order.getUserId().equals(userId)) {
			throw new RuntimeException("본인의 주문만 취소할 수 있습니다");
		}

		// completed는 이미 orders 테이블에 없으므로 PARTIAL/PENDING만 여기 도달
		if (order.getTradeType() == tradeType.BUY) {
			StockUser user = stockUserRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
			int refundAmount = order.getTradePrice() * order.getRemainingQuantity();
			user.setAsset(user.getAsset() + refundAmount);
			stockUserRepository.save(user);
		}

		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);
	}
}