package Poi.Stock.features.Order;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.HogaDTO;
import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.User.UserAssetService;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.features.kafka.KafkaProducer;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final KafkaProducer kafkaProducer;
	private final StockRepository stockRepository;
	private final OrderBookCache orderBookCache;
	private final WebSocketService webSocketService;
	private final CompletedOrderRepository completedOrderRepository;
	private final OrderTradeService orderTradeService;
	private final UserAssetService userAssetService; // ← StockUserRepository, HaveStockRepository 대체

	// StockUserRepository, HaveStockRepository, StockService 제거 ←

	/**
	 * 자산/보유주식 검증 추후 user-service 분리 시 UserAssetService → HTTP Client로 교체
	 */
	public void validateOrder(String userId, TradeDTO tradeDTO) {
		userAssetService.validateOrder(userId, tradeDTO.getTradeType(), tradeDTO.getStockCode(),
				tradeDTO.getTradePrice(), tradeDTO.getQuantity());
	}

	public void processOrder(TradeDTO tradeDTO) {
		Order order = orderTradeService.setOrder(tradeDTO);
		OrderBook book = orderBookCache.get(order.getStockCode());
		MatchingResult result = orderTradeService.processMatching(order, book);
		orderTradeService.sendHogaQuntityAndPrice(order.getStockCode(), result, book);
		Integer currentPrice = result.getLastExecutionPrice();
		webSocketService.SendCurrentPrice(order.getStockCode(), currentPrice);
		orderTradeService.updateStockPrice(order.getStockCode(), currentPrice);
	}

	public void placeOrder(String userId, TradeDTO tradeDTO) {
		tradeDTO.setUserId(userId);
		kafkaProducer.sendOrder(tradeDTO);
	}

	public Map<String, Object> getOrderHoga(String stockCode) {
		OrderBook orderBook = orderBookCache.get(stockCode);
		if (orderBook == null) {
			return Map.of("sellOrders", List.of(), "buyOrders", List.of());
		}
		return Map.of("sellOrders", getTopOrders(orderBook.getSellBook()), "buyOrders",
				getTopOrders(orderBook.getBuyBook()));
	}

	private List<HogaDTO> getTopOrders(NavigableMap<Integer, PriceLevel> book) {
		return book.entrySet().stream().limit(5).map(e -> new HogaDTO(e.getKey(), e.getValue().getTotalQuantity()))
				.toList();
	}

	/**
	 * 주문 취소 매수 취소 시 자산 환불 → UserAssetService 위임
	 */
	@Transactional
	public void cancelOrder(String userId, Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

		if (!order.getUserId().equals(userId)) {
			throw new RuntimeException("본인의 주문만 취소할 수 있습니다");
		}

		if (order.getTradeType() == tradeType.BUY) {
			int refundAmount = order.getTradePrice() * order.getRemainingQuantity();
			userAssetService.refundAsset(userId, refundAmount);
		}

		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);
	}
}