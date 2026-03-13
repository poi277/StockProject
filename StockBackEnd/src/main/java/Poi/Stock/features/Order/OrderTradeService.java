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
import Poi.Stock.features.Bot.Bot;
import Poi.Stock.features.Bot.BotCache;
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
	private final BotCache botCache;

	public Order setOrder(TradeDTO tradeDTO) {
		Order order = new Order();
		order.setUserId(tradeDTO.getUserId());
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

	@Transactional
	public MatchingResult processMatching(Order order, OrderBook book) {
		MatchingResult result = matchLoop(order, book);
		settleAll(result.getExecutions());
		saveTradeHistories(result.getExecutions());
		saveOrders(result, order);
		return result;
	}

	private MatchingResult matchLoop(Order order, OrderBook book) {
		MatchingResult result = new MatchingResult();
		TreeMap<Integer, PriceLevel> oppositeBook = order.getTradeType() == tradeType.BUY ? book.getSellBook()
				: book.getBuyBook();

		while (!order.isCompleted() && !oppositeBook.isEmpty()) {
			Integer firstPrice = oppositeBook.firstKey();
			boolean priceMatch = order.getTradeType() == tradeType.BUY ? firstPrice <= order.getTradePrice()
					: firstPrice >= order.getTradePrice();
			if (!priceMatch) break;

			PriceLevel level = oppositeBook.get(firstPrice);
			Order restingOrder = level.peek();
			int fillQty = Math.min(order.getRemainingQuantity(), restingOrder.getRemainingQuantity());

			order.decreaseRemainingQuantity(fillQty);
			restingOrder.decreaseRemainingQuantity(fillQty);
			level.reduceQuantity(fillQty);

			int fillPrice = restingOrder.getTradePrice();
			result.getMatchedPrices().add(fillPrice);

			String buyerId = order.getTradeType() == tradeType.BUY ? order.getUserId() : restingOrder.getUserId();
			String sellerId = order.getTradeType() == tradeType.BUY ? restingOrder.getUserId() : order.getUserId();

			result.getExecutions().add(new TradeExecution(order.getTradeType(), buyerId, sellerId, fillQty, fillPrice,
					order.getStockCode()));

			if (restingOrder.isCompleted()) {
				level.removeTopOrder();
				result.getCompletedResting().add(restingOrder);
			} else {
				result.getPartialResting().add(restingOrder);
			}
			if (level.isEmpty()) oppositeBook.remove(firstPrice);
		}

		if (!order.isCompleted()) {
			if (result.getExecutions().isEmpty()) {
				result.getMatchedPrices().add(order.getTradePrice());
			}
			book.addOrder(order);
		}
		return result;
	}

	private void settleAll(List<TradeExecution> executions) {
		if (executions.isEmpty()) return;

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

		applyAssetChanges(assetDelta, buyerExMap, sellerStockDelta, stockCode);
	}

	private void applyAssetChanges(Map<String, Integer> assetDelta,
			Map<String, List<TradeExecution>> buyerExMap,
			Map<String, Integer> sellerStockDelta, String stockCode) {

		// 봇 제외 유저만 필터링
		Set<String> userIds = assetDelta.keySet().stream()
				.filter(id -> !isBot(id))
				.collect(Collectors.toSet());

		if (userIds.isEmpty()) return;

		Map<String, StockUser> userMap = stockUserRepository.findAllById(userIds)
				.stream().collect(Collectors.toMap(StockUser::getId, u -> u));
		userMap.values().forEach(u -> u.setAsset(u.getAsset() + assetDelta.get(u.getId())));
		stockUserRepository.saveAll(userMap.values());

		Set<String> allUserIds = new HashSet<>();
		buyerExMap.keySet().stream().filter(id -> !isBot(id)).forEach(allUserIds::add);
		sellerStockDelta.keySet().stream().filter(id -> !isBot(id)).forEach(allUserIds::add);

		if (allUserIds.isEmpty()) return;

		Map<String, HaveStock> haveStockMap = haveStockRepository
				.findByUserIdsAndStockCode(allUserIds, stockCode)
				.stream().collect(Collectors.toMap(h -> h.getStockUser().getId(), h -> h));

		List<HaveStock> toSave = new ArrayList<>();

		for (Map.Entry<String, List<TradeExecution>> entry : buyerExMap.entrySet()) {
			if (isBot(entry.getKey())) continue;
			HaveStock hs = haveStockMap.computeIfAbsent(entry.getKey(), k -> {
				HaveStock h = new HaveStock();
				h.setStockUser(userMap.get(k));
				h.setStockCode(stockCode);
				h.setQuantity(0);
				return h;
			});
			entry.getValue().forEach(ex -> updateAveragePrice(hs, ex.getQuantity(), ex.getPrice()));
			toSave.add(hs);
		}

		for (Map.Entry<String, Integer> entry : sellerStockDelta.entrySet()) {
			if (isBot(entry.getKey())) continue;
			HaveStock hs = haveStockMap.get(entry.getKey());
			if (hs == null) throw new RuntimeException("매도자 보유 주식을 찾을 수 없습니다");
			hs.setQuantity(hs.getQuantity() + entry.getValue());
			toSave.add(hs);
		}

		haveStockRepository.saveAll(toSave);
	}

	private boolean isBot(String userId) {
		Bot bot = botCache.get(userId);
		return bot != null && bot.getBotType() != null;
	}

//	private void saveTradeHistories(List<TradeExecution> executions) {
//		if (executions.isEmpty())
//			return;
//		tradeHistoryRepository.saveAll(executions.stream().map(TradeHistory::from).toList());
//	}
	private void saveTradeHistories(List<TradeExecution> executions) {

		if (executions.isEmpty())
			return;

		List<TradeHistory> histories = executions.stream()
				.filter(ex -> !(isBot(ex.getBuyerId()) && isBot(ex.getSellerId()))).map(TradeHistory::from).toList();

		if (!histories.isEmpty()) {
			tradeHistoryRepository.saveAll(histories);
		}
	}

	private void saveOrders(MatchingResult result, Order incomingOrder) {
		boolean incomingIsBot = isBot(incomingOrder.getUserId());
		if (!result.getCompletedResting().isEmpty()) {
			// 체결(CompletedOrder)은 봇vs봇이면 저장 안함
			List<Order> completedToSave = result.getCompletedResting().stream()
					.filter(o -> !isBot(o.getUserId()) || !incomingIsBot).toList();
			if (!completedToSave.isEmpty()) {
				completedOrderRepository
						.saveAll(completedToSave.stream().map(CompletedOrder::setCompletedOrder).toList());
			}
			// 주문(Order) 삭제는 봇 포함 전부
			orderRepository.deleteAllInBatch(result.getCompletedResting());
		}
		if (!result.getPartialResting().isEmpty()) {
			// 주문(Order) 저장은 봇 포함 전부
			orderRepository.saveAll(result.getPartialResting());
		}
		if (incomingOrder.isCompleted()) {
			// 체결(CompletedOrder)은 봇vs봇이면 저장 안함
			if (!incomingIsBot || !result.getCompletedResting().stream().allMatch(o -> isBot(o.getUserId()))) {
				completedOrderRepository.save(CompletedOrder.setCompletedOrder(incomingOrder));
			}
			orderRepository.delete(incomingOrder);
		} else {
			orderRepository.save(incomingOrder);
		}
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
		for (int price : matchingResult.getMatchedPrices()) {
			PriceLevel sellLevel = book.getSellBook().get(price);
			PriceLevel buyLevel = book.getBuyBook().get(price);
			int sellQty = sellLevel == null ? 0 : sellLevel.getTotalQuantity();
			int buyQty = buyLevel == null ? 0 : buyLevel.getTotalQuantity();
			webSocketService.sendHoga(stockCode, tradeType.SELL, price, sellQty);
			webSocketService.sendHoga(stockCode, tradeType.BUY, price, buyQty);
		}
		for (TradeExecution execution : matchingResult.getExecutions()) {
			webSocketService.sendExecution(stockCode, execution);
		}
	}

	public void updateStockPrice(String stockCode, int currentPrice) {
		if (currentPrice <= 0)
			return;
		Stock stock = stockCache.get(stockCode);
		if (stock == null) return;
		stock.setClosePrice(currentPrice);
		if (stock.getHighPrice() == null || currentPrice > stock.getHighPrice()) {
			stock.setHighPrice(currentPrice);
		}
		if (stock.getLowPrice() == null || currentPrice < stock.getLowPrice()) {
			stock.setLowPrice(currentPrice);
		}
		if (stock.getOpenPrice() != null) {
			stock.setChangeAmount(currentPrice - stock.getOpenPrice());
			stock.setChangeRate((double) (currentPrice - stock.getOpenPrice()) / stock.getOpenPrice() * 100);
		}
	}
}