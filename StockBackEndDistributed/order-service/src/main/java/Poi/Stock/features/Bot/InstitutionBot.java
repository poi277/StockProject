package Poi.Stock.features.Bot;

import java.util.Map;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.MarketState;

@Component
public class InstitutionBot extends AbstractBot {

	public InstitutionBot(BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache,
			BotService botService, MarketStateHolder marketStateHolder, BotHaveStockCache botHaveStockCache,
			CandleCacheService candleCacheService, AssignedCodeHolder assignedCodeHolder) {
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder, botHaveStockCache,
				candleCacheService, assignedCodeHolder);
	}

	@Override
	protected String getBotId() {
		return "BOT_INSTITUTION";
	}

	@Override
	protected int getBotBaseIntensity() {
		return 40;
	}

	@Override
	protected int getBuyBase() {
		return 50;
	}

	@Override
	protected int getBuyRange() {
		return 50;
	}

	@Override
	protected int getSellBase() {
		return 200;
	}

	@Override
	protected int getSellRange() {
		return 300;
	}

	@Override
	protected int calculateBuyPrice(int currentPrice, int tickSize, int finalIntensity, int vix) {
		return currentPrice - (tickSize * random.nextInt(vix));
	}

	@Override
	protected int calculateSellPrice(int currentPrice, int tickSize, int finalIntensity, int vix) {
		return Math.max(tickSize, currentPrice + (tickSize * random.nextInt(vix)));
	}

	@Override
	public void placeOrders() {
		super.placeOrders();
	}

	@Override
	protected void executeTrade(StockRealTimeSnapshot stock, int currentPrice, int tickSize, MarketState state,
			int assetBonus, int finalIntensity) {
		String stockCode = stock.getStockCode();

		Map<Integer, Double> ma = getLatestMA(stockCode);
		double ma20 = ma.getOrDefault(20, 0.0);
		double ma60 = ma.getOrDefault(60, 0.0);
		double maMax = Math.max(ma20, ma60);

		if (shouldBuy(currentPrice, ma20, ma60, state, assetBonus)) {
			executeBuy(stock, currentPrice, tickSize, finalIntensity);
		} else {
			executeSell(stock, currentPrice, tickSize, maMax * 0.95, maMax * 0.97, maMax * 0.99, state, assetBonus,
					finalIntensity);
		}
	}

	private boolean shouldBuy(int currentPrice, double ma20, double ma60, MarketState state, int assetBonus) {
		if (state == MarketState.FLAT)
			return random.nextInt(100) < (30 + assetBonus);

		if (ma20 == 0.0 || ma60 == 0.0)
			return random.nextInt(100) < (30 + assetBonus);

		double maMin = Math.min(ma20, ma60);

		int buyProb = 0;
		if (currentPrice <= maMin * 1.01)
			buyProb = 60;
		else if (currentPrice <= maMin * 1.03)
			buyProb = 40;
		else if (currentPrice <= maMin * 1.05)
			buyProb = 20;

		if (buyProb == 0)
			return false;
		return random.nextInt(100) < (buyProb + assetBonus);
	}
}