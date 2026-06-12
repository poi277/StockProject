package Poi.Stock.features.Bot;

import java.util.Map;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.MarketState;

public class InstitutionBot extends AbstractBot {

	private final String botId;
	public InstitutionBot(String botId, BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache,
			BotService botService, MarketStateHolder marketStateHolder, BotHaveStockCache botHaveStockCache,
			CandleCacheService candleCacheService, AssignedCodeHolder assignedCodeHolder) {
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder, botHaveStockCache,
				candleCacheService, assignedCodeHolder);
		this.botId = botId; // 주입받은 ID 저장
	}

	@Override
	protected String getBotId() {
		return this.botId;
	}

	@Override
	public BotType getBotType() {
		return BotType.INSTITUTION;
	}

	@Override
	protected int getBuyBase() {
		return 5;
	}

	@Override
	protected int getSellBase() {
		return 5;
	}

	@Override
	protected int getBuyRange() {
		return 50;
	}

	@Override
	protected int getSellRange() {
		return 50;
	}

	@Override
	protected int getBotBaseIntensity() {
		return 40;
	}

	@Override
	protected int calculateBuyPrice(int currentPrice, int tickSize, int finalIntensity, int vix) {
		// VIX 지수를 0.5배로 줄여서 현재가 주변 호가창에 아주 촘촘하게 주문을 깔아둠
		int institutionVix = (int) (vix * 0.5);
		return super.calculateBuyPrice(currentPrice, tickSize, finalIntensity, institutionVix);
	}

	@Override
	protected int calculateSellPrice(int currentPrice, int tickSize, int finalIntensity, int vix) {
		int institutionVix = (int) (vix * 0.5);
		return super.calculateSellPrice(currentPrice, tickSize, finalIntensity, institutionVix);
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