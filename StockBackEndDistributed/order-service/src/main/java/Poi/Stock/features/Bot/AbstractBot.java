package Poi.Stock.features.Bot;

import java.util.Random;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractBot {

    protected final BotOrderService botOrderService;
    protected final BotCache botCache;
    protected final BotStockCache botStockCache;
    protected final BotService botService;
    protected final MarketStateHolder marketStateHolder;
    protected final BotHaveStockCache botHaveStockCache;
    protected final CandleCacheService candleCacheService;
    protected final AssignedCodeHolder assignedCodeHolder;

    protected final Random random = new Random();

    protected abstract String getBotId();
    protected abstract int getBotBaseIntensity();

	protected abstract void executeStrategy(StockRealTimeSnapshot stock);

    public void placeOrders() {
        String botId = getBotId();
        Bot bot = botCache.get(botId);
        if (bot == null) return;

        assignedCodeHolder.getAssignedCodes().forEach(stockCode -> {
            StockRealTimeSnapshot stock = botStockCache.get(stockCode);
            if (stock == null || stock.getCurrentPrice() <= 0) return;
			executeStrategy(stock);
        });
    }

	protected int getFinalIntensity(String stockCode) {
		int marketIntensity = marketStateHolder.getIntensity(stockCode);
		int botIntensity = getBotBaseIntensity();
		return (marketIntensity + botIntensity) / 2;
	}
}