package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

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
				result.addAll(level.getOrders()); // FIFO여야 함
			}
		} else {
			NavigableMap<Integer, PriceLevel> matchLevels = orderBook.getBuyBook().tailMap(order.getTradePrice(), true);
			for (PriceLevel level : matchLevels.values()) {
				result.addAll(level.getOrders());
			}
		}

		return result;
	}
	
	/**
	 * 체결 루프
	 */
	public Set<Integer> processMatching(Order order) {
		// 변동이 있는 가격의 WebSocket 전송용
		Set<Integer> matchedPrices = new HashSet<>();
		matchedPrices.add(order.getTradePrice());
		// 해당 종목의 OrderBook 조회
		OrderBook book = orderBookCache.get(order.getStockCode());
		// 남은 수량이 있는 동안 반복 (부분 체결 대응)
		while (order.getRemainingQuantity() > 0) {
			// 내 주문의 반대편 호가창 선택 매수면 매도호가(sellBook) 매도면 매수호가(buyBook)
			TreeMap<Integer, PriceLevel> oppositeBook = order.getTradeType() == tradeType.BUY ? book.getSellBook()
					: book.getBuyBook();
			// 반대편에 주문이 하나도 없으면 체결 종료
			if (oppositeBook.isEmpty())
				break;
			// 가장 유리한 가격 선택
			Integer bestPrice = oppositeBook.firstKey();
			// 가격 조건이 체결 가능한지 확인 매수: 상대 가격 ≤ 내 매수가격 매도: 상대 가격 ≥ 내 매도가격
			boolean priceMatch = order.getTradeType() == tradeType.BUY ? bestPrice <= order.getTradePrice()
					: bestPrice >= order.getTradePrice();
			// 가격이 안 맞으면 더 이상 체결 불가 → 종료
			if (!priceMatch)
				break;
			PriceLevel level = oppositeBook.get(bestPrice);
			// 해당 가격 레벨의 가장 먼저 들어온 주문(FIFO) 가져오기
			Order restingOrder = level.peek();
			// 실제 체결 수량 계산 (둘 중 작은 값)
			int fillQty = Math.min(order.getRemainingQuantity(), restingOrder.getRemainingQuantity());
			// 내 주문 수량 감소
			order.decreaseRemainingQuantity(fillQty);
			// 상대 주문 수량 감소
			restingOrder.decreaseRemainingQuantity(fillQty);
			// 해당 가격 레벨의 총 수량 감소
			level.reduceQuantity(fillQty);
			// 체결 가격은 기존 대기 주문(resting order)의 가격을 사용
			int fillPrice = restingOrder.getTradePrice();
			// 체결 가격 기록
			matchedPrices.add(fillPrice);
			// 상대 주문이 전량 체결된 경우
			if (restingOrder.getRemainingQuantity() == 0) {
				// 큐에서 제거 (FIFO)
				level.removeTopOrder();
				// DB 이동 또는 완료 처리
				OrderSaveDB(restingOrder);
			}
			// 해당 가격 레벨에 더 이상 주문이 없다면
			if (level.isEmpty()) {
				// 가격 레벨 자체 제거 (TreeMap에서 삭제)
				oppositeBook.remove(bestPrice);
			}
			// 실제 자산 이동 및 보유주식 갱신 처리
			settle(order, restingOrder, fillQty, fillPrice);
		}

		// 내 주문 상태 최종 갱신

		// 전량 체결
		if (order.getRemainingQuantity() == 0) {
			order.setStatus(OrderStatus.COMPLETED);
			// 일부 체결
		} else if (order.getRemainingQuantity() < order.getQuantity()) {
			order.setStatus(OrderStatus.PARTIAL);
		}
		// 남은 수량이 있으면 Book에 등록
		if (order.getRemainingQuantity() > 0) {
			book.addOrder(order);
		}
		OrderSaveDB(order);
		// 체결된 가격 목록 반환
		return matchedPrices;
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

	public void OrderSaveDB(Order order) {
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
		webSocketService.SendCurrentPrice(stockCode, book.getSellfirstKey());
	}
}
