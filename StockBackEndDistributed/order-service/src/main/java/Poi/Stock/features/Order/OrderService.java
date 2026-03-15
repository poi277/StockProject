package Poi.Stock.features.Order;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import Poi.Stock.DTO.user.HogaDTO;
import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.features.kafka.KafkaProducer;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.repository.OrderRepository;
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
    private final OrderBookCache orderBookCache;
    private final WebSocketService webSocketService;
    private final CompletedOrderRepository completedOrderRepository;
    private final OrderTradeService orderTradeService;
    private final RestTemplate restTemplate;

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

    @Transactional
    public void cancelOrder(String userId, Long orderId, String accessToken) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("본인의 주문만 취소할 수 있습니다");
        }

        if (order.getTradeType() == tradeType.BUY) {
            int refundAmount = order.getTradePrice() * order.getRemainingQuantity();
            // user-service에 환불 요청
            String url = userServiceUrl + "/user/refund";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(Map.of("refundAmount", refundAmount), headers), Void.class);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
