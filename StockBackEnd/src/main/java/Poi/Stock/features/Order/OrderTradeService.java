package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

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

	public Order setOrder(String userId, TradeDTO tradeDTO) {
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
		List<Order> result = new ArrayList<>();
		if (order.getTradeType() == tradeType.BUY) {
			NavigableMap<Integer, PriceLevel> matchLevels = orderBook.getSellBook().headMap(order.getTradePrice(),
					true);
			for (PriceLevel level : matchLevels.values()) {
				result.addAll(level.getOrders());
			}
		} else {
			NavigableMap<Integer, PriceLevel> matchLevels = orderBook.getBuyBook().headMap(order.getTradePrice(), true);

			for (PriceLevel level : matchLevels.values()) {
				result.addAll(level.getOrders());
			}
		}

		return result;
	}
	
	/**
	 * 체결 루프
	 */
	public Set<Integer> processMatching(Order order, List<Order> matchOrderList) {
		Set<Integer> changedPrices = new HashSet<>();
		for (Order opposite : matchOrderList) {

			if (order.getRemainingQuantity() == 0)
				break;
			int fillQty = Math.min(order.getRemainingQuantity(), opposite.getRemainingQuantity());
			int fillPrice = opposite.getTradePrice();
			order.setRemainingQuantity(order.getRemainingQuantity() - fillQty);
			opposite.setRemainingQuantity(opposite.getRemainingQuantity() - fillQty);

			order.setStatus(order.getRemainingQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);
			opposite.setStatus(opposite.getRemainingQuantity() == 0 ? OrderStatus.COMPLETED : OrderStatus.PARTIAL);

			saveOrComplete(opposite);
			settle(order, opposite, fillQty, fillPrice);

			// 🔥 변경된 가격 기록
			changedPrices.add(fillPrice);
		}

		return changedPrices;
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

		// 매수자 보유주식 찾기
		HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(buyer, newOrder.getStockCode())
				.orElseGet(() -> {
					HaveStock hs = new HaveStock();
					hs.setStockUser(buyer);
					hs.setStockCode(newOrder.getStockCode());
					hs.setQuantity(0);
					return hs;
				});
		// 매수자의 주식 증가
		updateAveragePrice(haveStock, fillQty, fillPrice);
		haveStockRepository.save(haveStock);
	}

	private void updateAveragePrice(HaveStock haveStock, int fillQty, int fillPrice) {
		if (haveStock.getQuantity() == 0) {
			haveStock.setAveragePrice(fillPrice);
		} else {
			double totalCost = haveStock.getAveragePrice() * haveStock.getQuantity() + (double) fillPrice * fillQty;
			haveStock.setAveragePrice(totalCost / (haveStock.getQuantity() + fillQty));
		}
		haveStock.setQuantity(haveStock.getQuantity() + fillQty);
	}

	public void saveOrComplete(Order order) {
		if (order.getStatus() == OrderStatus.COMPLETED) {
			completedOrderRepository.save(CompletedOrder.from(order));
			orderRepository.delete(order);
			OrderBook book = orderBookCache.get(order.getStockCode());
			book.removeOrder(order);
		} else {
			orderRepository.save(order);
		}
	}

	public int getLowestSellPrice(String stockCode) {
		OrderBook book = orderBookCache.get(stockCode);
		return book.getSellBook().isEmpty() ? 0 : book.getSellBook().firstKey();
	}

	public void sendDeltaForPrice(String stockCode, Set<Integer> changedPrices) {
		OrderBook book = orderBookCache.get(stockCode);
		for (int price : changedPrices) {
			PriceLevel sellLevel = book.getSellBook().get(price);
			PriceLevel buyLevel = book.getBuyBook().get(price);
			int sellQty = sellLevel == null ? 0 : sellLevel.getTotalQuantity();
			int buyQty = buyLevel == null ? 0 : buyLevel.getTotalQuantity();
			webSocketService.sendDelta(stockCode, tradeType.SELL, price, sellQty);
			webSocketService.sendDelta(stockCode, tradeType.BUY, price, buyQty);
		}
	}
}
