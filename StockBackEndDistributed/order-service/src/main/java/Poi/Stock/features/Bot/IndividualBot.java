package Poi.Stock.features.Bot;

import java.util.Map;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.MarketState;

public class IndividualBot extends AbstractBot {

	private final String botId;

	public IndividualBot(String botId, BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache,
			BotService botService, MarketStateHolder marketStateHolder, BotHaveStockCache botHaveStockCache,
			CandleCacheService candleCacheService, AssignedCodeHolder assignedCodeHolder) {
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder, botHaveStockCache,
				candleCacheService, assignedCodeHolder);
		this.botId = botId;
	}

	@Override
	protected String getBotId() {
		return this.botId;
	}

	@Override
	public BotType getBotType() {
		return BotType.INDIVIDUAL;
	}

	@Override
	protected int getBotBaseIntensity() {
		return 60;
	}

	@Override
	protected int getBuyBase() {
		return 1;
	}

	@Override
	protected int getSellBase() {
		return 1;
	}

	@Override
	protected int getBuyRange() {
		return 10;
	}

	@Override
	protected int getSellRange() {
		return 10;
	}

	@Override
	public void placeOrders() {
		super.placeOrders();
	}

	@Override
	protected int calculateBuyPrice(int currentPrice, int tickSize, int finalIntensity, int vix) {
		// VIX 지수를 1.2배 뻥튀기하여 훨씬 더 난폭하고 넓은 범위로 호가를 던지게 만듦
		int individualVix = (int) (vix * 1.2);
		return super.calculateBuyPrice(currentPrice, tickSize, finalIntensity, individualVix);
	}

	@Override
	protected int calculateSellPrice(int currentPrice, int tickSize, int finalIntensity, int vix) {
		int individualVix = (int) (vix * 1.2);
		return super.calculateSellPrice(currentPrice, tickSize, finalIntensity, individualVix);
	}

	@Override
	protected void executeTrade(StockRealTimeSnapshot stock, int currentPrice, int tickSize, MarketState state,
			int assetBonus, int finalIntensity) {
		String stockCode = stock.getStockCode();

		Map<Integer, Double> ma = getLatestMA(stockCode);
		double ma5 = ma.getOrDefault(5, 0.0);
		double ma20 = ma.getOrDefault(20, 0.0);
		double ma60 = ma.getOrDefault(60, 0.0);

		if (shouldBuy(currentPrice, ma5, ma20, ma60, state, assetBonus)) {
			executeBuy(stock, currentPrice, tickSize, finalIntensity);
		} else {
			executeSell(stock, currentPrice, tickSize, ma5, ma20, ma60, state, assetBonus, finalIntensity);
		}
	}

	private boolean shouldBuy(int currentPrice, double ma5, double ma20, double ma60, MarketState state,
			int assetBonus) {
		int buyProb = 0;
		if (state == MarketState.FLAT)
			buyProb = 30;
		else if (currentPrice < ma60)
			buyProb = 60;
		else if (currentPrice < ma20)
			buyProb = 40;
		else if (currentPrice < ma5)
			buyProb = 20;

		if (buyProb == 0)
			return false;
		return random.nextInt(100) < (buyProb + assetBonus);
	}
}