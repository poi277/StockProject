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
import Poi.Stock.DTO.user.myOrderDTO;
import Poi.Stock.features.Candle.CandleService;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.features.kafka.KafkaProducer;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.OrderRepository;
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

    @Value("${user.service.url}")
    private String userServiceUrl;

    /**
     * 자산/보유주식 검증 — user-service HTTP 호출
     */
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

    @Transactional
    public void processOrder(TradeDTO tradeDTO) {
        Order order = orderTradeService.setOrder(tradeDTO);
        OrderBook book = orderBookCache.get(order.getStockCode());
        MatchingResult result = orderTradeService.matchLoop(order, book);
        orderTradeService.saveTradeHistories(result.getExecutions());
        orderTradeService.saveOrders(result, order);
		orderTradeService.settlement(result);
		orderTradeService.updateStockCache(order.getStockCode(), result);
		orderTradeService.updateCurrentCandle(order.getStockCode(), result);
        orderTradeService.sendWebSocket(order.getStockCode(), result, book);
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

	public List<myOrderDTO> getMyOrder(String userId) {
		List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
		return orders.stream().map(order -> {
			myOrderDTO dto = new myOrderDTO();
			dto.setOrderId(order.getOrderId());
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
}
