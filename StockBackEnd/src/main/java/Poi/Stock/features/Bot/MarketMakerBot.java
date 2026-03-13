package Poi.Stock.features.Bot;

import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Order.OrderCancelService;
import Poi.Stock.features.Websocket.StockCache;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketMakerBot {

	private final BotOrderService botOrderService;
	private final BotCache botCache;
	private final StockCache stockCache;
	private final OrderCancelService orderCancelService;

	private static final String BOT_ID = "BOT_MARKET_MAKER";
	private static final int hogaLevel = 1;
	private static final int[][] QUANTITY_RANGE = { { 500, 1000 }, // 1단계
			{ 300, 500 }, // 2단계
			{ 100, 300 }, // 3단계
			{ 50, 100 }, // 4단계
			{ 10, 50 } // 5단계
	};

	private final Random random = new Random();

	@Scheduled(fixedDelay = 5000)
	public void placeOrders() {

		Bot bot = botCache.get(BOT_ID);
		if (bot == null)
			return;

		var stock = stockCache.get("035420");
		if (stock == null)
			return;

		int currentPrice = stock.getClosePrice();
		if (currentPrice <= 0)
			return;

		// BUY / SELL 랜덤
		tradeType type = random.nextBoolean() ? tradeType.BUY : tradeType.SELL;

		// 가격은 현재가 그대로
		int price = currentPrice;

		// 수량 랜덤
		int quantity = 100 + random.nextInt(901);

		botOrderService.placeOrder(BOT_ID, "035420", type, price, quantity);
	}
}