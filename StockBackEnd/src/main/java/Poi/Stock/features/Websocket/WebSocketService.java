package Poi.Stock.features.Websocket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import Poi.Stock.features.Order.Order; // ✅ 이게 맞음
import Poi.Stock.features.Order.OrderBook;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

	private final StockRepository stockRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final StockCache stockCache;
	private final OrderBookCache orderBookCache;
	private final OrderRepository orderRepository;
	// 웹소켓을 위해 메모리에 주식 정보 저장 (종목코드별 최신 데이터)

	// 서버 시작시 DB에서 각 종목의 최신 데이터만 로드
	@PostConstruct
	public void init() {

		List<Stock> latestStocks = stockRepository.findLatestStocks();

		latestStocks.forEach(stock -> {

			String stockCode = stock.getStockCode();

			stockCache.put(stockCode, stock);

			// 미체결 상태 목록
			List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.PARTIAL);

			// 매도 호가
			List<Order> sellOrders = orderRepository
					.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceAscPriorityAsc(stockCode, tradeType.SELL,
							activeStatuses);

			// 매수 호가
			List<Order> buyOrders = orderRepository
					.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceDescPriorityAsc(stockCode, tradeType.BUY,
							activeStatuses);

			OrderBook orderBook = new OrderBook();
			orderBook.setSellOrders(sellOrders);
			orderBook.setBuyOrders(buyOrders);

			orderBookCache.put(stockCode, orderBook);

			log.info("호가 초기화 완료: {} (sell {}, buy {})", stockCode, sellOrders.size(), buyOrders.size());
		});

		log.info("주식 {} 개 및 호가 캐시 로드 완료", latestStocks.size());
	}


	// 10분마다 DB에 저장 (새로운 날짜 레코드로 저장)
	@Scheduled(fixedRate = 600000)
	public void saveToDatabase() {
		LocalDate today = LocalDate.now();

		// 각 종목의 현재 상태를 오늘 날짜로 저장
		List<Stock> stocksToSave = new ArrayList<>();

		for (Stock cachedStock : stockCache.values()) {
			Stock newRecord = new Stock();
			newRecord.setStockCode(cachedStock.getStockCode());
			newRecord.setDate(today);
			newRecord.setStockName(cachedStock.getStockName());
			newRecord.setOpenPrice(cachedStock.getOpenPrice());
			newRecord.setHighPrice(cachedStock.getHighPrice());
			newRecord.setLowPrice(cachedStock.getLowPrice());
			newRecord.setClosePrice(cachedStock.getClosePrice());
			newRecord.setVolume(cachedStock.getVolume());
			newRecord.setValue(cachedStock.getValue());
			newRecord.setChangeAmount(cachedStock.getChangeAmount());
			newRecord.setChangeRate(cachedStock.getChangeRate());

			stocksToSave.add(newRecord);
		}

		stockRepository.saveAll(stocksToSave);
		log.info("DB 저장 완료 - {} 건", stocksToSave.size());
	}

//	-------------------------------
	// 가격 업데이트 및 WebSocket 전송
	public void updateCurrentPrice(String stockCode, int currentPrice) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("stockCode", stockCode);
		payload.put("currentPrice", currentPrice);
		messagingTemplate.convertAndSend("/topic/stock/" + stockCode, payload);
	}

	public void updateWebsocketHoga(String stockCode) {
		OrderBook orderBook = orderBookCache.get(stockCode);

		// 가격별 합산
		Map<Integer, Integer> sellMap = new LinkedHashMap<>();
		for (Order o : orderBook.getSellOrders()) {
			sellMap.merge(o.getTradePrice(), o.getRemainingQuantity(), Integer::sum);
		}
		Map<Integer, Integer> buyMap = new LinkedHashMap<>();
		for (Order o : orderBook.getBuyOrders()) {
			buyMap.merge(o.getTradePrice(), o.getRemainingQuantity(), Integer::sum);
		}

		List<Map<String, Object>> sellList = sellMap.entrySet().stream()
				.map(e -> {
					Map<String, Object> m = new HashMap<>();
					m.put("tradePrice", e.getKey());
					m.put("remainingQuantity", e.getValue());
					return m;
				}).collect(Collectors.toList());

		List<Map<String, Object>> buyList = buyMap.entrySet().stream().map(e -> {
			Map<String, Object> m = new HashMap<>();
			m.put("tradePrice", e.getKey());
			m.put("remainingQuantity", e.getValue());
			return m;
		}).collect(Collectors.toList());

		Map<String, Object> payload = new HashMap<>();
		payload.put("sellOrders", sellList);
		payload.put("buyOrders", buyList);
		messagingTemplate.convertAndSend("/topic/hoga/" + stockCode, payload);
	}
}
