package Poi.Stock.features.Bot;

import java.util.Map;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.MarketState;

public class ForeignBot extends AbstractBot {

	private final String botId;

	public ForeignBot(String botId, BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache,
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
		return BotType.FOREIGN;
	}

	@Override
	protected int getBuyBase() {
		return 3;
	}

	@Override
	protected int getSellBase() {
		return 3;
	}

	@Override
	protected int getBuyRange() {
		return 30;
	}

	@Override
	protected int getSellRange() {
		return 30;
	}

	@Override
	public void placeOrders() {
		super.placeOrders();
	}

	@Override
	protected int getBotBaseIntensity() {
		return 60;
	} // 중간 성향

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
			return random.nextInt(100) < (20 + assetBonus); // 횡보장엔 소극적

		int buyProb = 0;
		if (state == MarketState.BULL) {
			// 추세 추종: 오르고 있을 때 매수
			if (currentPrice > ma60 && ma20 > ma60)
				buyProb = 60; // MA60 위 + 정배열
			else if (currentPrice > ma20)
				buyProb = 40; // MA20 위
			else if (currentPrice > ma60)
				buyProb = 20; // MA60 위
		} else {
			// 하락장엔 매수 소극적
			buyProb = 10;
		}

		if (buyProb == 0)
			return false;
		return random.nextInt(100) < (buyProb + assetBonus);
	}
}