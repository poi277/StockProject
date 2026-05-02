package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.TreadeHistory.TradeHistory;
import Poi.Stock.features.Bot.Bot;
import Poi.Stock.features.Bot.BotCache;
import Poi.Stock.features.Candle.CandleService;
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
import Poi.Stock.shared.event.SettlementEvent.haveStockChange;
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
	private final SettlementProducer settlementProducer;
	private final CandleService candleService;

	// StockUserRepository, HaveStockRepository 제거

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
		order.setStockName(stockCache.get(tradeDTO.getStockCode()).getStockName());
		return order;
	}

	public void settlement(MatchingResult result) {
		if (!result.getExecutions().isEmpty()) {
			SettlementEvent event = buildSettlementEvent(result.getExecutions());
			settlementProducer.sendSettlement(event);
		}
	}
	private SettlementEvent buildSettlementEvent(List<TradeExecution> executions) {
		String stockCode = executions.get(0).getStockCode();
		Map<String, Integer> assetDelta = new HashMap<>();
		List<haveStockChange> stockChanges = new ArrayList<>();

		for (TradeExecution ex : executions) {
			boolean buyerIsBot = isBot(ex.getBuyerId());
			boolean sellerIsBot = isBot(ex.getSellerId());
			int total = ex.getPrice() * ex.getQuantity();

			// ✅ 여기 추가
			log.info("buyerId={}, sellerId={}, price={}, qty={}, total={}, buyerIsBot={}, sellerIsBot={}",
					ex.getBuyerId(), ex.getSellerId(), ex.getPrice(), ex.getQuantity(), total, buyerIsBot, sellerIsBot);

			if (!buyerIsBot) {
				stockChanges.add(new haveStockChange(ex.getBuyerId(), stockCode, ex.getQuantity(), ex.getPrice()));
				assetDelta.merge(ex.getBuyerId(), -total, Integer::sum);
			}
			if (!sellerIsBot) {
				stockChanges.add(new haveStockChange(ex.getSellerId(), stockCode, -ex.getQuantity(), ex.getPrice()));
				assetDelta.merge(ex.getSellerId(), total, Integer::sum);
			}
		}

		// ✅ 여기도 추가
		log.info("assetDelta={}, stockChanges={}", assetDelta, stockChanges);

		List<AssetChange> assetChanges = assetDelta.entrySet().stream()
				.map(e -> new AssetChange(e.getKey(), e.getValue())).toList();
		return new SettlementEvent(stockCode, assetChanges, stockChanges);
	}

	public MatchingResult matchLoop(Order order, OrderBook book, Stock stock) {
		MatchingResult result = new MatchingResult();
		// 거래량은 아직 정산이 안되기 떄문에 여기서 임의로 증가시켜줘야함
		Long fillTotalvolume = stock.getTotalvolume();
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
			fillTotalvolume += fillQty;
			order.decreaseRemainingQuantity(fillQty);
			restingOrder.decreaseRemainingQuantity(fillQty);
			level.reduceQuantity(fillQty);
			int fillPrice = restingOrder.getTradePrice();
			result.getMatchedPrices().add(fillPrice);
			String buyerId = order.getTradeType() == tradeType.BUY ? order.getUserId() : restingOrder.getUserId();
			String sellerId = order.getTradeType() == tradeType.BUY ? restingOrder.getUserId() : order.getUserId();
			result.getExecutions().add(new TradeExecution(order.getTradeType(), buyerId, sellerId, fillQty, fillPrice,
					order.getStockCode(), stock.calcChangeRate(fillPrice), fillTotalvolume,
					LocalDateTime.now()));
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
		result.setIncomingOrder(order);
		return result;
	}

	public void saveTradeHistories(List<TradeExecution> executions) {
		if (executions.isEmpty()) return;
		List<TradeHistory> histories = executions.stream()
				.filter(ex -> !(isBot(ex.getBuyerId()) && isBot(ex.getSellerId()))).map(TradeHistory::from).toList();
		if (!histories.isEmpty()) {
			tradeHistoryRepository.saveAll(histories);
		}
	}

	public void saveOrders(MatchingResult result, Order incomingOrder) {
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

	public void sendWebSocket(MatchingResult matchingResult, OrderBook book, Stock stock) {
		for (int price : matchingResult.getMatchedPrices()) {
			PriceLevel sellLevel = book.getSellBook().get(price);
			PriceLevel buyLevel = book.getBuyBook().get(price);
			int sellQty = sellLevel == null ? 0 : sellLevel.getTotalQuantity();
			int buyQty = buyLevel == null ? 0 : buyLevel.getTotalQuantity();
			webSocketService.sendHoga(stock.getStockCode(), tradeType.SELL, price, sellQty);
			webSocketService.sendHoga(stock.getStockCode(), tradeType.BUY, price, buyQty);
		}
		for (TradeExecution execution : matchingResult.getExecutions()) {
			webSocketService.sendExecution(stock.getStockCode(), execution);
		}
		LocalDateTime tradeTime = matchingResult.getLastExecutionTime();
		webSocketService.SendCurrentPrice(stock, tradeTime);
		webSocketService.sendOrderUpdate(stock, matchingResult);
	}

	public void updateStockCache(String stockCode, MatchingResult result) {
		Integer currentPrice = result.getLastExecutionPrice();
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
			stock.setChangeRate(stock.calcChangeRate(currentPrice));
		}
		stock.setTotalvolume(stock.getTotalvolume() + result.getTotalFilledQty());
		System.out.println(stock.getTotalvolume());
	}

	public void updateCurrentCandle(String stockCode, MatchingResult result) {
		Integer currentPrice = result.getLastExecutionPrice();
		int filledQty = result.getTotalFilledQty();
		LocalDateTime lastExecutiontime = result.getLastExecutionTime();
		if (currentPrice != null && currentPrice > 0 && lastExecutiontime != null) {
			candleService.updateCandle(stockCode, currentPrice, filledQty, lastExecutiontime);
		}
	}

	public void saveEditOrders(MatchingResult result, Order incomingOrder) {
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
			completedOrderRepository.save(CompletedOrder.setCompletedOrder(incomingOrder));
			orderRepository.delete(incomingOrder);
		} else {
			orderRepository.save(incomingOrder);
		}
	}
}