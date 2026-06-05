package Poi.Stock.features.Bot;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.EnumUtil.tradeType;

@Component
public class IndividualBot extends AbstractBot {

    public IndividualBot(BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache, 
                         BotService botService, MarketStateHolder marketStateHolder) {
        super(botOrderService, botCache, botStockCache, botService, marketStateHolder);
    }

    @Override
    protected String getBotId() {
        return "BOT_INDIVIDUAL";
    }

	// @Scheduled(fixedDelay = 10000)
    @Override
    public void placeOrders() {
        super.placeOrders();
    }

    @Override
	protected void executeStrategy(boolean isBuy, StockRealTimeSnapshot stock) {
		int currentPrice = stock.getYesterdayClosePrice();
        int tickSize = stock.getTickSize(currentPrice);

		int quantity = 1 + random.nextInt(10);

        if (isBuy) {
			int price = currentPrice + tickSize - (tickSize * random.nextInt(marketStateHolder.peoplevix()));
            if (botService.canBuy(getBotId(), price, quantity)) {
                botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(), tradeType.BUY, price, quantity);
            }
        } else {
			int price = currentPrice - tickSize + (tickSize * random.nextInt(marketStateHolder.peoplevix()));
            if (price > 0 && botService.canSell(getBotId(), stock.getStockCode(), quantity)) {
                botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(), tradeType.SELL, price, quantity);
            }
        }
    }
}