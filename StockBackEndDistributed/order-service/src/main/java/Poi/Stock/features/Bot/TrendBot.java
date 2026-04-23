package Poi.Stock.features.Bot;

import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrendBot {

	private final BotOrderService botOrderService;
	private final BotCache botCache;
	private final StockCache stockCache;

	private static final String BOT_ID = "BOT_TREND";

	private final Random random = new Random();

	@Scheduled(fixedDelay = 2000)
	public void placeOrders() {
		System.out.println("trade");
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

		tradeType type = random.nextBoolean() ? tradeType.BUY : tradeType.SELL;

		int price;

		if (type == tradeType.BUY) {
			price = currentPrice + tickSize;
		} else {
			price = currentPrice - tickSize;
		}

		int quantity = 200 + random.nextInt(500);

		if (price > 0) {
			botOrderService.placeOrder(BOT_ID, "035420", type, price, quantity);
		}
	}
}