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
	private static final int hogaLevel = 5;
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

		stockCache.getCache().forEach((stockCode, stock) -> {
			int currentPrice = stock.getClosePrice();
			if (currentPrice <= 0)
				return;

			// 기존 주문 취소 먼저
			orderCancelService.cancelAllOrders(BOT_ID, stockCode);

			for (int i = 1; i <= hogaLevel; i++) {
				int min = QUANTITY_RANGE[i - 1][0];
				int max = QUANTITY_RANGE[i - 1][1];
				int quantity = min + random.nextInt(max - min + 1);

				int tickSize = stock.getTickSize(currentPrice);
				int sellPrice = currentPrice + (tickSize * i);
				int buyPrice = currentPrice - (tickSize * i);

				botOrderService.placeOrder(BOT_ID, stockCode, tradeType.SELL, sellPrice, quantity);
				if (buyPrice > 0) {
					botOrderService.placeOrder(BOT_ID, stockCode, tradeType.BUY, buyPrice, quantity);
				}
			}
		});
	}
}