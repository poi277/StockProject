package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.CompletedOrder.CompletedOrder;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.features.Websocket.OrderBookCache;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.repository.CompletedOrderRepository;
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
public class OrderTradeService {

	private final OrderRepository orderRepository;
	private final OrderBookCache orderBookCache;
	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;
	private final WebSocketService webSocketService;
	private final CompletedOrderRepository completedOrderRepository;

	public Order buildOrder(String userId, TradeDTO tradeDTO) {
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
		return order;
	}

	public List<Order> findMatchOrderList(Order order) {
		OrderBook orderBook = orderBookCache.get(order.getStockCode());

		return order.getTradeType() == tradeType.BUY
				? orderBook.getSellOrders().stream().filter(o -> o.getTradePrice() <= order.getTradePrice())
						.sorted(Comparator.comparingInt(Order::getTradePrice).thenComparingLong(Order::getPriority))
						.collect(Collectors.toList())
				: orderBook.getBuyOrders().stream().filter(o -> o.getTradePrice() >= order.getTradePrice()).sorted(
						Comparator.comparingInt(Order::getTradePrice).reversed().thenComparingLong(Order::getPriority))
						.collect(Collectors.toList());
	}
	/**
	 * 체결 루프 - 마지막 체결 가격 반환 (체결 없으면 0)
	 */
	public int processMatching(Order order, List<Order> matchOrderList) {
		// 남은 매도 최저가
		int lastFillPrice = 0;

		for (Order opposite : matchOrderList) {
			if (order.getRemainingQuantity() == 0)
				break;
			// 리스트와 주문에서 남은 주식만큼 체결
			int fillQty = Math.min(order.getRemainingQuantity(), opposite.getRemainingQuantity());
			int fillPrice = opposite.getTradePrice();
			lastFillPrice = fillPrice;

			order.setRemainingQuantity(order.getRemainingQuantity() - fillQty);
			opposite.setRemainingQuantity(opposite.getRemainingQuantity() - fillQty);

			order.setStatus(order.getRemainingQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);
			opposite.setStatus(opposite.getRemainingQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);

			// 신규 주문 저장
			saveOrComplete(opposite);

			// 자산 및 보유주식 정산
			settle(order, opposite, fillQty, fillPrice);
		}

		return lastFillPrice;
	}

	private void settle(Order newOrder, Order opposite, int fillQty, int fillPrice) {
		String buyerId = newOrder.getTradeType() == tradeType.BUY ? newOrder.getUserId() : opposite.getUserId();
		String sellerId = newOrder.getTradeType() == tradeType.BUY ? opposite.getUserId() : newOrder.getUserId();
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
		HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(buyer, newOrder.getStockCode())
				.orElseGet(() -> {
					HaveStock hs = new HaveStock();
					hs.setStockUser(buyer);
					hs.setStockCode(newOrder.getStockCode());
					hs.setQuantity(0);
					return hs;
				});
		haveStock.setQuantity(haveStock.getQuantity() + fillQty);
		haveStockRepository.save(haveStock);
	}

	public void saveOrComplete(Order order) {
		if (order.getStatus() == OrderStatus.COMPLETED) {
			completedOrderRepository.save(CompletedOrder.from(order));
			orderRepository.delete(order);
			orderBookCache.removeOrder(order);
		} else {
			orderRepository.save(order);
		}
	}
}
