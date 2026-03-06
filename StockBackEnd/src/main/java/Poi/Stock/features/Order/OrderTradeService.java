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
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.TreadeHistory.TradeHistory;
import Poi.Stock.features.CompletedOrder.CompletedOrder;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.features.Websocket.OrderBookCache;
import Poi.Stock.features.Websocket.StockCache;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.object.TradeExecution;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.repository.TradeHistoryRepository;
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
	private final StockCache stockCache;
	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;
	private final WebSocketService webSocketService;
	private final CompletedOrderRepository completedOrderRepository;
	private final TradeHistoryRepository tradeHistoryRepository;

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
	 * 
	 * @param book
	 */
	@Transactional
	public MatchingResult processMatching(Order order, OrderBook book) {
		Set<Integer> matchedPrices = new HashSet<>();
		// 지금의 가격도 웹소켓 변동에 전송해야하니 추가
		matchedPrices.add(order.getTradePrice());
		List<TradeExecution> executions = new ArrayList<>();
		TreeMap<Integer, PriceLevel> oppositeBook = order.getTradeType() == tradeType.BUY ? book.getSellBook()
				: book.getBuyBook();
		// 매칭되는것들의 주식처리
		matchLoop(order, oppositeBook, matchedPrices, executions);
		// 결제 프로세스
		settleAll(executions);
		saveOrder(order, book);
		return new MatchingResult(matchedPrices, executions);
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
			// 최종 주문 체결이 되는 양
			int fillQty = Math.min(order.getRemainingQuantity(), restingOrder.getRemainingQuantity());

			order.decreaseRemainingQuantity(fillQty);
			restingOrder.decreaseRemainingQuantity(fillQty);
			level.reduceQuantity(fillQty);

			int fillPrice = restingOrder.getTradePrice();
			matchedPrices.add(fillPrice);

			String buyerId = order.getTradeType() == tradeType.BUY ? order.getUserId() : restingOrder.getUserId();
			String sellerId = order.getTradeType() == tradeType.BUY ? restingOrder.getUserId() : order.getUserId();
			executions.add(new TradeExecution(order.getTradeType(), buyerId, sellerId, fillQty, fillPrice,
					order.getStockCode()));
			// 데이터베이스에 저장
			saveRestingOrder(restingOrder, level, oppositeBook, firstPrice);
		}
	}

	private void saveRestingOrder(Order restingOrder, PriceLevel level, TreeMap<Integer, PriceLevel> oppositeBook,
			int firstPrice) {
		if (restingOrder.isCompleted()) {
			level.removeTopOrder();
			completedOrderRepository.save(CompletedOrder.setCompletedOrder(restingOrder));
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
			completedOrderRepository.save(CompletedOrder.setCompletedOrder(order));
		} else {
			orderRepository.save(order);
			book.addOrder(order);
		}
	}

	private void settleAll(List<TradeExecution> executions) {
		if (executions.isEmpty())
			return;

		String stockCode = executions.get(0).getStockCode();
		Map<String, Integer> assetDelta = new HashMap<>();
		Map<String, List<TradeExecution>> buyerExMap = new HashMap<>();
		Map<String, Integer> sellerStockDelta = new HashMap<>();
		for (TradeExecution ex : executions) {
			int total = ex.getPrice() * ex.getQuantity();
			assetDelta.merge(ex.getBuyerId(), -total, Integer::sum);
			assetDelta.merge(ex.getSellerId(), total, Integer::sum);
			buyerExMap.computeIfAbsent(ex.getBuyerId(), k -> new ArrayList<>()).add(ex);
			sellerStockDelta.merge(ex.getSellerId(), -ex.getQuantity(), Integer::sum);
		}
		// 유저 한 번 조회
		Map<String, StockUser> userMap = stockUserRepository.findAllById(assetDelta.keySet()).stream()
				.collect(Collectors.toMap(StockUser::getId, u -> u));
		// 자산 반영
		userMap.values().forEach(u -> u.setAsset(u.getAsset() + assetDelta.get(u.getId())));
		stockUserRepository.saveAll(userMap.values());
		// 보유주식 한 번 조회
		Set<String> allUserIds = new HashSet<>();
		allUserIds.addAll(buyerExMap.keySet());
		allUserIds.addAll(sellerStockDelta.keySet());
		Map<String, HaveStock> haveStockMap = haveStockRepository.findByUserIdsAndStockCode(allUserIds, stockCode)
				.stream().collect(Collectors.toMap(h -> h.getStockUser().getId(), h -> h));
		List<HaveStock> toSave = new ArrayList<>();
		// 매수자 - averagePrice 계산
		for (Map.Entry<String, List<TradeExecution>> entry : buyerExMap.entrySet()) {
			HaveStock hs = haveStockMap.computeIfAbsent(entry.getKey(), k -> {
				HaveStock h = new HaveStock();
				h.setStockUser(userMap.get(k));
				h.setStockCode(stockCode);
				h.setQuantity(0);
				return h;
			});
			for (TradeExecution ex : entry.getValue()) {
				updateAveragePrice(hs, ex.getQuantity(), ex.getPrice());
			}
			toSave.add(hs);
		}
		// 매도자 - 수량만 감소
		for (Map.Entry<String, Integer> entry : sellerStockDelta.entrySet()) {
			HaveStock hs = haveStockMap.get(entry.getKey());
			if (hs == null)
				throw new RuntimeException("매도자 보유 주식을 찾을 수 없습니다");
			hs.setQuantity(hs.getQuantity() + entry.getValue()); // 음수 합산
			toSave.add(hs);
		}
		haveStockRepository.saveAll(toSave);
		List<TradeHistory> histories = executions.stream().map(TradeHistory::from).collect(Collectors.toList());
		tradeHistoryRepository.saveAll(histories);
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

	public void sendHogaQuntityAndPrice(String stockCode, MatchingResult matchingResult, OrderBook book) {
		// 1. 호가 잔량 전송 (기존 코드)
		for (int price : matchingResult.getMatchedPrices()) {
			PriceLevel sellLevel = book.getSellBook().get(price);
			PriceLevel buyLevel = book.getBuyBook().get(price);
			int sellQty = sellLevel == null ? 0 : sellLevel.getTotalQuantity();
			int buyQty = buyLevel == null ? 0 : buyLevel.getTotalQuantity();
			webSocketService.sendHoga(stockCode, tradeType.SELL, price, sellQty);
			webSocketService.sendHoga(stockCode, tradeType.BUY, price, buyQty);
		}
		// 2. 체결 내역 전송
		for (TradeExecution execution : matchingResult.getExecutions()) {
			webSocketService.sendExecution(stockCode, execution);
		}
	}

	public void updateStockPrice(String stockCode, int currentPrice) {
		Stock stock = stockCache.get(stockCode);
		if (stock == null)
			return;
		// 종가 업데이트
		stock.setClosePrice(currentPrice);
		// 고가 업데이트
		if (stock.getHighPrice() == null || currentPrice > stock.getHighPrice()) {
			stock.setHighPrice(currentPrice);
		}
		// 저가 업데이트
		if (stock.getLowPrice() == null || currentPrice < stock.getLowPrice()) {
			stock.setLowPrice(currentPrice);
		}

		// 변동폭/변동률 업데이트
		if (stock.getOpenPrice() != null) {
			stock.setChangeAmount(currentPrice - stock.getOpenPrice());
			stock.setChangeRate((double) (currentPrice - stock.getOpenPrice()) / stock.getOpenPrice() * 100);
		}
	}
}
