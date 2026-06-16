package Poi.Stock.features.Bot;

import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.Lock.StockLock;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.features.Order.OrderService;
import Poi.Stock.features.Order.OrderTradeService;
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
	private final WebSocketService webSocketService;
	private final StockLock stockLock;
	private final OrderService orderService;

	public void placeOrder(String botId, String stockCode, String stockName, tradeType type, int price, int quantity) {
		stockLock.lock(stockCode);
		try {
			TradeDTO tradeDTO = new TradeDTO();
			long fakeOrderId = -Math.abs(java.util.UUID.randomUUID().getMostSignificantBits());
			tradeDTO.setOrderId(fakeOrderId); // TradeDTO에 orderId 세터가 있다면 세팅!
			tradeDTO.setStockCode(stockCode);
			tradeDTO.setUserId(botId);
			tradeDTO.setTradeType(type);
			tradeDTO.setTradePrice(price);
			tradeDTO.setQuantity(quantity);
			tradeDTO.setStockName(stockName);
			orderService.processOrder(tradeDTO);
		} catch (Exception e) {
			log.error("봇 주문 처리 실패: {} / {}", botId, e.getMessage(), e);
		} finally {
			stockLock.unlock(stockCode);
		}
	}
}