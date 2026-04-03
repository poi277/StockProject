package Poi.Stock.features.Bot;

import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Lock.StockLock;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.features.Order.OrderService;
import Poi.Stock.features.Order.OrderTradeService;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotOrderService {

	private final OrderTradeService orderTradeService;
	private final OrderBookCache orderBookCache;
	private final StockCache stockCache;
	private final WebSocketService webSocketService;
	private final StockLock stockLock;
	private final OrderService orderService;

	public void placeOrder(String botId, String stockCode, tradeType type, int price, int quantity) {
		stockLock.lock(stockCode);
		try {
			TradeDTO tradeDTO = new TradeDTO();
			tradeDTO.setStockCode(stockCode);
			tradeDTO.setUserId(botId);
			tradeDTO.setTradeType(type);
			tradeDTO.setTradePrice(price);
			tradeDTO.setQuantity(quantity);
			orderService.processOrder(tradeDTO);
		} catch (Exception e) {
			log.error("봇 주문 처리 실패: {} / {}", botId, e.getMessage(), e);
		} finally {
			stockLock.unlock(stockCode);
		}
	}
}