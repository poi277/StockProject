package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
	/**
	 * 체결 루프
	 */
	public Set<Integer> processMatching(Order order) {
		Set<Integer> matchedPrices = new HashSet<>();
		matchedPrices.add(order.getTradePrice());
		List<TradeExecution> executions = new ArrayList<>();
		OrderBook book = orderBookCache.get(order.getStockCode());
		TreeMap<Integer, PriceLevel> oppositeBook = order.getTradeType() == tradeType.BUY ? book.getSellBook()
				: book.getBuyBook();

		matchLoop(order, oppositeBook, matchedPrices, executions);
		settleAll(executions);
		saveOrder(order, book);

		return matchedPrices;
	}

	private void matchLoop(Order order, TreeMap<Integer, PriceLevel> oppositeBook, Set<Integer> matchedPrices,
			List<TradeExecution> executions) {
		while (order.getRemainingQuantity() > 0) {
			if (oppositeBook.isEmpty())
				break;
			Integer firstPrice = oppositeBook.firstKey();
			boolean priceMatch = order.getTradeType() == tradeType.BUY ? firstPrice <= order.getTradePrice()
					: firstPrice >= order.getTradePrice();
			if (!priceMatch)
				break;

			PriceLevel level = oppositeBook.get(firstPrice);
			Order restingOrder = level.peek();
			int fillQty = Math.min(order.getRemainingQuantity(), restingOrder.getRemainingQuantity());

			order.decreaseRemainingQuantity(fillQty);
			restingOrder.decreaseRemainingQuantity(fillQty);
			level.reduceQuantity(fillQty);

			int fillPrice = restingOrder.getTradePrice();
			matchedPrices.add(fillPrice);

			String buyerId = order.getTradeType() == tradeType.BUY ? order.getUserId() : restingOrder.getUserId();
			String sellerId = order.getTradeType() == tradeType.BUY ? restingOrder.getUserId() : order.getUserId();
			executions.add(new TradeExecution(buyerId, sellerId, fillQty, fillPrice, order.getStockCode()));

			saveRestingOrder(restingOrder, level, oppositeBook, firstPrice);
		}
	}

	private void saveRestingOrder(Order restingOrder, PriceLevel level, TreeMap<Integer, PriceLevel> oppositeBook,
			int firstPrice) {
		if (restingOrder.isCompleted()) {
			level.removeTopOrder();
			completedOrderRepository.save(CompletedOrder.from(restingOrder));
			orderRepository.delete(restingOrder);
		} else {
			orderRepository.save(restingOrder);
		}
		if (level.isEmpty()) {
			oppositeBook.remove(firstPrice);
		}
	}

	private void saveOrder(Order order, OrderBook book) {
		if (order.isCompleted()) {
			completedOrderRepository.save(CompletedOrder.from(order));
		} else {
			orderRepository.save(order);
			book.addOrder(order);
		}
	}

	private void settleAll(List<TradeExecution> executions) {
		if (executions.isEmpty())
			return;

		Map<String, Integer> assetDelta = new HashMap<>();
		Map<String, List<TradeExecution>> buyerExecutions = new HashMap<>();
		Map<String, Map<String, Integer>> sellerStockDelta = new HashMap<>();

		for (TradeExecution ex : executions) {
			int totalAmount = ex.getPrice() * ex.getQuantity();
			assetDelta.merge(ex.getBuyerId(), -totalAmount, Integer::sum);
			assetDelta.merge(ex.getSellerId(), totalAmount, Integer::sum);
			buyerExecutions.computeIfAbsent(ex.getBuyerId(), k -> new ArrayList<>()).add(ex);
			sellerStockDelta.computeIfAbsent(ex.getSellerId(), k -> new HashMap<>()).merge(ex.getStockCode(),
					-ex.getQuantity(), Integer::sum);
		}
		List<StockUser> users = stockUserRepository.findAllById(assetDelta.keySet());
		Map<String, StockUser> userMap = users.stream().collect(Collectors.toMap(StockUser::getId, u -> u));


		for (Map.Entry<String, Integer> entry : assetDelta.entrySet()) {
			userMap.get(entry.getKey()).setAsset(userMap.get(entry.getKey()).getAsset() + entry.getValue());
		}
		stockUserRepository.saveAll(users);

		// 한 번에 조회
		String stockCode = executions.get(0).getStockCode(); // 단일 종목
		Set<String> allUserIds = new HashSet<>();
		allUserIds.addAll(buyerExecutions.keySet());
		allUserIds.addAll(sellerStockDelta.keySet());

		Map<String, HaveStock> haveStockMap = haveStockRepository.findByUserIdsAndStockCode(allUserIds, stockCode)
				.stream().collect(Collectors.toMap(h -> h.getStockUser().getId(), h -> h));

		// 매수자 주식 반영
		List<HaveStock> toSave = new ArrayList<>();
		for (Map.Entry<String, List<TradeExecution>> entry : buyerExecutions.entrySet()) {
			StockUser buyer = userMap.get(entry.getKey());
			HaveStock hs = haveStockMap.computeIfAbsent(entry.getKey(), k -> {
				HaveStock h = new HaveStock();
				h.setStockUser(buyer);
				h.setStockCode(stockCode);
				h.setQuantity(0);
				return h;
			});
			for (TradeExecution ex : entry.getValue()) {
				updateAveragePrice(hs, ex.getQuantity(), ex.getPrice());
			}
			toSave.add(hs);
		}

		// 매도자 주식 반영
		for (Map.Entry<String, Map<String, Integer>> sellerEntry : sellerStockDelta.entrySet()) {
			HaveStock hs = haveStockMap.get(sellerEntry.getKey());
			if (hs == null)
				throw new RuntimeException("매도자 보유 주식을 찾을 수 없습니다");
			for (Map.Entry<String, Integer> stockEntry : sellerEntry.getValue().entrySet()) {
				hs.setQuantity(hs.getQuantity() + stockEntry.getValue());
			}
			toSave.add(hs);
		}

		haveStockRepository.saveAll(toSave); // 한 번에 저장
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
