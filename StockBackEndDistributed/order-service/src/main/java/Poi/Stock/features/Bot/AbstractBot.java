package Poi.Stock.features.Bot;

import java.util.List;
import java.util.Map;
import java.util.Random;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.CandleType;
import Poi.Stock.util.EnumUtil.MarketState;
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

	protected abstract void executeStrategy(boolean isBuy, StockRealTimeSnapshot stock, Map<Integer, Double> ma);

	public void placeOrders() {
		String botId = getBotId();
		Bot bot = botCache.get(botId);
		if (bot == null)
			return;
		assignedCodeHolder.getAssignedCodes().forEach(stockCode -> {
			StockRealTimeSnapshot stock = botStockCache.get(stockCode);
			if (stock == null || stock.getCurrentPrice() <= 0)
				return;
			List<CandleWithMA<CandleMinute>> candles = candleCacheService.getCandles(CandleType.ONE_MINUTE, stockCode);
			if (candles == null || candles.isEmpty())
				return;
			Map<Integer, Double> ma = candles.get(candles.size() - 1).getMa();
			if (ma == null || ma.isEmpty())
				return;

			double ma5 = ma.getOrDefault(5, 0.0);
			double ma20 = ma.getOrDefault(20, 0.0);
			double ma60 = ma.getOrDefault(60, 0.0);
			if (ma5 == 0.0 || ma20 == 0.0 || ma60 == 0.0)
				return;
			int currentPrice = stock.getCurrentPrice();
			MarketState state = marketStateHolder.getState(stockCode);

			boolean isBuy;
			if (state == MarketState.BULL || state == MarketState.BEAR) {
				isBuy = currentPrice < ma5 || currentPrice < ma20 || currentPrice < ma60;
			} else {
				isBuy = currentPrice < ma20;
			}
			executeStrategy(isBuy, stock, ma);
		});
	}
}