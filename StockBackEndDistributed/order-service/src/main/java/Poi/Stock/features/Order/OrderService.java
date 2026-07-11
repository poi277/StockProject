package Poi.Stock.features.Order;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import Poi.Stock.DTO.user.HogaDTO;
import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.DTO.user.myAllOrderDTO;
import Poi.Stock.DTO.user.myStockOrderDTO;
import Poi.Stock.features.Bot.BotService;
import Poi.Stock.features.Candle.CandleService;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.features.kafka.KafkaProducer;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaProducer kafkaProducer;
    private final OrderBookCache orderBookCache;
    private final WebSocketService webSocketService;
    private final CompletedOrderRepository completedOrderRepository;
    private final OrderTradeService orderTradeService;
    private final RestTemplate restTemplate;
	private final CandleService candleService;
	private final StockCache stockCache;
	private final BotService botService;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public void validateOrder(String userId, TradeDTO tradeDTO, String accessToken) {
        String url = userServiceUrl + "/user/validate-order";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "tradeType", tradeDTO.getTradeType().name(),
            "stockCode", tradeDTO.getStockCode(),
            "tradePrice", tradeDTO.getTradePrice(),
            "quantity", tradeDTO.getQuantity()
        );

        try {
            restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

	public Order validateEditOrder(String userId, TradeDTO tradeDTO, String accessToken) {
		Order order = orderRepository.findById(tradeDTO.getOrderId()).orElseThrow(() -> new RuntimeException("주문 없음"));
		if (!order.getUserId().equals(userId)) {
			throw new RuntimeException("본인 주문이 아닙니다");
		}
		if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PARTIAL) {
			throw new RuntimeException("대기/부분체결 주문만 수정 가능합니다");
		}
		String url = userServiceUrl + "/user/validate-editOrder";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		Map<String, Object> body = Map.of("tradeType", order.getTradeType().name(), "stockCode",
				tradeDTO.getStockCode(), "newPrice", tradeDTO.getTradePrice(), "oldPrice", order.getTradePrice(),
				"newQuantity", tradeDTO.getQuantity(), "RemainingQuantity", order.getRemainingQuantity()
		);
		try {
			restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Void.class);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return order;
	}

    @Transactional
    public void processOrder(TradeDTO tradeDTO) {
		Order order = orderTradeService.setOrder(tradeDTO);
		OrderBook book = orderBookCache.get(order.getStockCode());
		MatchingResult result = orderTradeService.matchLoop(order, book);
		orderTradeService.saveTradeHistories(result.getExecutions());
		orderTradeService.saveOrders(result);
		orderTradeService.settlement(result);
		orderTradeService.updateCurrentCandle(result);
		orderTradeService.sendWebSocket(result, book);
		botService.setBotStockCache(result);
    }

	public String validateOrder(String stockCode)
    {
		StockRealTimeSnapshot stock = stockCache.get(stockCode);
		return stock.getStockName();
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
        return Map.of(
            "sellOrders", getTopOrders(orderBook.getSellBook()),
            "buyOrders",  getTopOrders(orderBook.getBuyBook()));
    }

    private List<HogaDTO> getTopOrders(NavigableMap<Integer, PriceLevel> book) {
        return book.entrySet().stream()
            .limit(5)
            .map(e -> new HogaDTO(e.getKey(), e.getValue().getTotalQuantity()))
            .toList();
    }

	public List<myAllOrderDTO> getMyAllStockOrder(String userId) {
		List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
		return orders.stream().map(order -> {
			myAllOrderDTO dto = new myAllOrderDTO();
			dto.setOrderId(order.getOrderId());
			dto.setStockCode(order.getStockCode());
			dto.setStockName(order.getStockName());
			dto.setTradeType(order.getTradeType());
			dto.setQuantity(order.getQuantity());
			dto.setRemainingQuantity(order.getRemainingQuantity());
			dto.setTradePrice(order.getTradePrice());
			dto.setStatus(order.getStatus());
			dto.setCreatedAt(order.getCreatedAt());
			return dto;
		}).collect(Collectors.toList());
	}

	public List<myStockOrderDTO> getMyStockOrder(String userId, String stockCode) {
		List<Order> orders = orderRepository.findByUserIdAndStockCodeOrderByCreatedAtDesc(userId, stockCode);
		return orders.stream().map(order -> {
			myStockOrderDTO dto = new myStockOrderDTO();
			dto.setOrderId(order.getOrderId());
			dto.setStockCode(order.getStockCode());
			dto.setTradeType(order.getTradeType());
			dto.setQuantity(order.getQuantity());
			dto.setRemainingQuantity(order.getRemainingQuantity());
			dto.setTradePrice(order.getTradePrice());
			dto.setStatus(order.getStatus());
			dto.setCreatedAt(order.getCreatedAt());
			return dto;
		}).collect(Collectors.toList());
	}

	@Transactional
	public void stockEdit(TradeDTO tradeDTO, Order order) {
		Integer oldPrice = order.getTradePrice();

	    OrderBook book = orderBookCache.get(order.getStockCode());
	    book.removeOrder(order);

		int filledQty = order.getQuantity() - order.getRemainingQuantity();

		if (tradeDTO.getQuantity() < filledQty) {
			throw new RuntimeException("수정 수량이 이미 체결된 수량보다 작습니다");
		}

	    order.setTradePrice(tradeDTO.getTradePrice());
	    order.setQuantity(tradeDTO.getQuantity());
		order.setRemainingQuantity(tradeDTO.getQuantity() - filledQty);
	    order.setPriority(System.nanoTime());

		MatchingResult result = orderTradeService.matchLoop(order, book);

		result.getMatchedPrices().add(oldPrice);

		orderTradeService.saveTradeHistories(result.getExecutions());

	    orderTradeService.saveEditOrders(result, order);

	    orderTradeService.settlement(result);
		orderTradeService.updateCurrentCandle(result);
		orderTradeService.sendWebSocket(result, book);
	}
}