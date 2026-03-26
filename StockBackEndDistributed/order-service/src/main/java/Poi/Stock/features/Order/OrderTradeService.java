package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.TreadeHistory.TradeHistory;
import Poi.Stock.features.Bot.Bot;
import Poi.Stock.features.Bot.BotCache;
import Poi.Stock.features.CompletedOrder.CompletedOrder;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.features.kafka.SettlementProducer;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.object.TradeExecution;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.TradeHistoryRepository;
import Poi.Stock.shared.event.SettlementEvent;
import Poi.Stock.shared.event.SettlementEvent.AssetChange;
import Poi.Stock.shared.event.SettlementEvent.StockChange;
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
	private final WebSocketService webSocketService;
	private final CompletedOrderRepository completedOrderRepository;
	private final TradeHistoryRepository tradeHistoryRepository;
	private final BotCache botCache;
	private final SettlementProducer settlementProducer; // ← 추가

	// StockUserRepository, HaveStockRepository 제거 ←

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

		// settleAll() 제거 → Kafka 이벤트 발행으로 교체
		if (!result.getExecutions().isEmpty()) {
			SettlementEvent event = buildSettlementEvent(result.getExecutions());
			settlementProducer.sendSettlement(event);
		}

		saveTradeHistories(result.getExecutions());
		saveOrders(result, order);
		return result;
	}

	/**
	 * 기존 settleAll + applyAssetChanges 로직을 SettlementEvent 빌드로 변환 봇 간 거래는 이벤트에서 제외
	 */
	private SettlementEvent buildSettlementEvent(List<TradeExecution> executions) {
		String stockCode = executions.get(0).getStockCode();
		Map<String, Integer> assetDelta = new HashMap<>();
		List<StockChange> stockChanges = new ArrayList<>();
		for (TradeExecution ex : executions) {
			boolean buyerIsBot = isBot(ex.getBuyerId());
			boolean sellerIsBot = isBot(ex.getSellerId());
			int total = ex.getPrice() * ex.getQuantity();
			if (!buyerIsBot) {
				// 매수자: 현금 차감 제거 (주문 시 이미 차감됨)
				// 주식 수량 증가만
				stockChanges.add(new StockChange(ex.getBuyerId(), stockCode, ex.getQuantity(), ex.getPrice()));
			}
			if (!sellerIsBot) {
				// 매도자: 현금 증가만
				assetDelta.merge(ex.getSellerId(), total, Integer::sum);
				// 주식 수량 감소 제거 (주문 시 이미 차감됨)
			}
		}
		List<AssetChange> assetChanges = assetDelta.entrySet().stream()
				.map(e -> new AssetChange(e.getKey(), e.getValue())).toList();
		return new SettlementEvent(stockCode, assetChanges, stockChanges);
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

	private void saveTradeHistories(List<TradeExecution> executions) {
		if (executions.isEmpty()) return;
		List<TradeHistory> histories = executions.stream()
				.filter(ex -> !(isBot(ex.getBuyerId()) && isBot(ex.getSellerId()))).map(TradeHistory::from).toList();
		if (!histories.isEmpty()) {
			tradeHistoryRepository.saveAll(histories);
		}
	}

	private void saveOrders(MatchingResult result, Order incomingOrder) {
		boolean incomingIsBot = isBot(incomingOrder.getUserId());
		if (!result.getCompletedResting().isEmpty()) {
			List<Order> completedToSave = result.getCompletedResting().stream()
					.filter(o -> !isBot(o.getUserId()) || !incomingIsBot).toList();
			if (!completedToSave.isEmpty()) {
				completedOrderRepository
						.saveAll(completedToSave.stream().map(CompletedOrder::setCompletedOrder).toList());
			}
			orderRepository.deleteAllInBatch(result.getCompletedResting());
		}
		if (!result.getPartialResting().isEmpty()) {
			orderRepository.saveAll(result.getPartialResting());
		}
		if (incomingOrder.isCompleted()) {
			if (!incomingIsBot || !result.getCompletedResting().stream().allMatch(o -> isBot(o.getUserId()))) {
				completedOrderRepository.save(CompletedOrder.setCompletedOrder(incomingOrder));
			}
			orderRepository.delete(incomingOrder);
		} else {
			orderRepository.save(incomingOrder);
		}
	}

	private boolean isBot(String userId) {
		Bot bot = botCache.get(userId);
		return bot != null && bot.getBotType() != null;
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

	public void updateStockPrice(String stockCode, Integer currentPrice) {
		if (currentPrice == null || currentPrice <= 0)
			return;
		Stock stock = stockCache.get(stockCode);
		if (stock == null)
			return;
		stock.setClosePrice(currentPrice);
		if (stock.getHighPrice() == null || currentPrice > stock.getHighPrice())
			stock.setHighPrice(currentPrice);
		if (stock.getLowPrice() == null || currentPrice < stock.getLowPrice())
			stock.setLowPrice(currentPrice);
		if (stock.getOpenPrice() != null) {
			stock.setChangeAmount(currentPrice - stock.getOpenPrice());
			stock.setChangeRate((double) (currentPrice - stock.getOpenPrice()) / stock.getOpenPrice() * 100);
		}
	}
}