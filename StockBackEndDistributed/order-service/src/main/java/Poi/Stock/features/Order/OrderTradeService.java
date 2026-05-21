package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Bot.Bot;
import Poi.Stock.features.Bot.BotCache;
import Poi.Stock.features.Candle.CandleService;
import Poi.Stock.features.CompletedOrder.CompletedOrder;
import Poi.Stock.features.TradeHistory.TradeHistory;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.features.kafka.SettlementProducer;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.object.SettlementEvent;
import Poi.Stock.object.SettlementEvent.haveStockChange;
import Poi.Stock.object.TradeExecution;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.OrderRepository;
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
		order.setStockName(tradeDTO.getStockName());
		return order;
	}

	public void settlement(MatchingResult result) {
		if (!result.getExecutions().isEmpty()) {

			SettlementEvent event = buildSettlementEvent(result.getExecutions(), result.getStockCode());
			// userservice에 자산 업데이트
			settlementProducer.sendSettlement(event);
			// stockservice에 거래량 및 현재가 업데이트
			settlementProducer.sendTradeExecutionStockService(result.getExecutions());
		}
	}

	private SettlementEvent buildSettlementEvent(List<TradeExecution> executions, String stockCode) {
		List<haveStockChange> stockChanges = new ArrayList<>();
		for (TradeExecution ex : executions) {
			if (!isBot(ex.getBuyerId()))
			    stockChanges.add(new haveStockChange(ex.getBuyerId(), ex.getQuantity(), ex.getPrice()));
			if (!isBot(ex.getSellerId()))
			    stockChanges.add(new haveStockChange(ex.getSellerId(), -ex.getQuantity(), ex.getPrice()));
		}
		return new SettlementEvent(stockCode, stockChanges);
	}

	public MatchingResult matchLoop(Order order, OrderBook book) {
		MatchingResult result = new MatchingResult(order.getStockCode());
		// 거래량은 아직 정산이 안되기 떄문에 여기서 임의로 증가시켜줘야함
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
			result.getExecutions()
					.add(new TradeExecution(order.getTradeType(), order.getStatus(), buyerId, sellerId, fillQty,
							fillPrice,
					order.getStockCode(), LocalDateTime.now()));
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

	public void saveOrders(MatchingResult result) {
		boolean incomingIsBot = isBot(result.getIncomingOrder().getUserId());
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
		if (result.getIncomingOrder().isCompleted()) {
			if (!incomingIsBot || !result.getCompletedResting().stream().allMatch(o -> isBot(o.getUserId()))) {
				completedOrderRepository.save(CompletedOrder.setCompletedOrder(result.getIncomingOrder()));
			}
			orderRepository.delete(result.getIncomingOrder());
		} else {
			orderRepository.save(result.getIncomingOrder());
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

	public void sendWebSocket(MatchingResult matchingResult, OrderBook book) {
		for (int price : matchingResult.getMatchedPrices()) {
			PriceLevel sellLevel = book.getSellBook().get(price);
			PriceLevel buyLevel = book.getBuyBook().get(price);
			int sellQty = sellLevel == null ? 0 : sellLevel.getTotalQuantity();
			int buyQty = buyLevel == null ? 0 : buyLevel.getTotalQuantity();
			webSocketService.sendHoga(matchingResult.getStockCode(), tradeType.SELL, price, sellQty);
			webSocketService.sendHoga(matchingResult.getStockCode(), tradeType.BUY, price, buyQty);
		}
		webSocketService.sendOrderUpdate(matchingResult);
	}


	public void updateCurrentCandle(MatchingResult result) {
		Integer currentPrice = result.getLastExecutionPrice();
		LocalDateTime lastExecutiontime = result.getLastExecutionTime();

		if (currentPrice != null && currentPrice > 0 && lastExecutiontime != null) {
			candleService.saveCandleOrder(result.getStockCode(), currentPrice, result.getBuyFilledQty(),
					result.getSellFilledQty(), result.getTotalTradeAmount(), lastExecutiontime);
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