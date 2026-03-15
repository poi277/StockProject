package Poi.Stock.features.Bot;

import java.util.Random;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Order.OrderCancelService;
import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
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

	// @Scheduled(fixedDelay = 3000)
	public void placeOrders() {

		Bot bot = botCache.get(BOT_ID);
		if (bot == null)
			return;

		Stock stock = stockCache.get("035420");
		if (stock == null)
			return;

		int currentPrice = stock.getClosePrice();
		if (currentPrice <= 0)
			return;

		int tickSize = stock.getTickSize(currentPrice);

		for (int i = 1; i <= hogaLevel; i++) {

			int min = QUANTITY_RANGE[i - 1][0];
			int max = QUANTITY_RANGE[i - 1][1];
			int quantity = min + random.nextInt(max - min + 1);

			int sellPrice = currentPrice + (tickSize * i);
			int buyPrice = currentPrice - (tickSize * i);

			// BUY / SELL 랜덤 선택
			tradeType type = random.nextBoolean() ? tradeType.BUY : tradeType.SELL;

			if (type == tradeType.SELL) {

				botOrderService.placeOrder(BOT_ID, "035420", tradeType.SELL, sellPrice, quantity);

			} else {

				if (buyPrice > 0) {
					botOrderService.placeOrder(BOT_ID, "035420", tradeType.BUY, buyPrice, quantity);
				}

			}
		}
		orderCancelService.cancelOutOfRange(BOT_ID, "035420", currentPrice, tickSize, 4);
	}
}