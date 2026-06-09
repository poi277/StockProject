package Poi.Stock.features.Bot;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.CandleType;
import Poi.Stock.util.EnumUtil.MarketState;
import Poi.Stock.util.EnumUtil.tradeType;

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
	public void placeOrders() {
		super.placeOrders();
	}

	@Override
	protected void executeStrategy(StockRealTimeSnapshot stock) {
		String stockCode = stock.getStockCode();
		int currentPrice = stock.getCurrentPrice();
		int tickSize = stock.getTickSize(currentPrice);

		// 공통 데이터 준비 - 기관은 20, 60 이평선만 사용
		double ma20 = 0.0, ma60 = 0.0;
		List<CandleWithMA<CandleMinute>> candles = candleCacheService.getCandles(CandleType.ONE_MINUTE, stockCode);
		if (candles != null && !candles.isEmpty()) {
			Map<Integer, Double> ma = candles.get(candles.size() - 1).getMa();
			if (ma != null) {
				ma20 = ma.getOrDefault(20, 0.0);
				ma60 = ma.getOrDefault(60, 0.0);
			}
		}

		MarketState state = marketStateHolder.getState(stockCode);
		int finalIntensity = getFinalIntensity(stockCode);
		Bot bot = botCache.get(getBotId());
		int assetBonus = (int) Math.min(30, bot.getAsset() / 1_000_000);

		if (shouldBuy(currentPrice, ma20, ma60, state, assetBonus)) {
			executeBuy(stock, currentPrice, tickSize, finalIntensity);
		} else {
			executeSell(stock, currentPrice, tickSize, ma20, ma60, state, finalIntensity, assetBonus);
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
			buyProb = 30;
		else if (currentPrice <= maMin * 1.03)
			buyProb = 20;
		else if (currentPrice <= maMin * 1.05)
			buyProb = 10;

		return (buyProb > 0) && (random.nextInt(100) < (buyProb + assetBonus));
	}

	private void executeBuy(StockRealTimeSnapshot stock, int currentPrice, int tickSize, int finalIntensity) {
		String stockCode = stock.getStockCode();
		int quantity = calculateQuantity(50, 50, finalIntensity);
		int price = currentPrice - tickSize;
		if (botService.canBuy(getBotId(), price, quantity)) {
			botOrderService.placeOrder(getBotId(), stockCode, stock.getStockName(), tradeType.BUY, price, quantity);
		}
	}

	private void executeSell(StockRealTimeSnapshot stock, int currentPrice, int tickSize, double ma20, double ma60,
			MarketState state, int finalIntensity, int assetBonus) {
		String stockCode = stock.getStockCode();
		BotHaveStock haveStock = botHaveStockCache.get(getBotId(), stockCode);
		if (haveStock == null || haveStock.getQuantity() <= 0)
			return;

		double avgPrice = haveStock.getAveragePrice();
		double profitRate = (currentPrice - avgPrice) / avgPrice * 100;
		double maMax = Math.max(ma20, ma60);

		int sellProb = 0;
		if (state == MarketState.FLAT) {
			sellProb = 30;
		} else if (state == MarketState.BULL) {
			if (currentPrice >= maMax * 0.99)
				sellProb = 60 + (int) Math.min(30, profitRate * 5);
			else if (currentPrice >= maMax * 0.97)
				sellProb = 40 + (int) Math.min(20, profitRate * 3);
			else if (currentPrice >= maMax * 0.95)
				sellProb = 20 + (int) Math.min(10, profitRate * 2);
		} else {
			double lossRate = -profitRate;
			if (currentPrice >= maMax * 0.99)
				sellProb = 60 + (int) Math.min(30, lossRate * 5);
			else if (currentPrice >= maMax * 0.97)
				sellProb = 40 + (int) Math.min(20, lossRate * 3);
			else if (currentPrice >= maMax * 0.95)
				sellProb = 20 + (int) Math.min(10, lossRate * 2);
		}

		sellProb = (int) (sellProb * (finalIntensity / 100.0));

		if ((sellProb > 0) && (random.nextInt(100) < (sellProb + assetBonus))) {
			int quantity = calculateQuantity(200, 300, finalIntensity);
			quantity = Math.min(quantity, haveStock.getQuantity());
			int price = currentPrice + tickSize;
			if (botService.canSell(getBotId(), stockCode, quantity)) {
				botOrderService.placeOrder(getBotId(), stockCode, stock.getStockName(), tradeType.SELL, price,
						quantity);
			}
		}
	}

	private int calculateQuantity(int base, int range, int intensity) {
		double intensityMultiplier = 1.0 + (intensity / 100.0);
		int baseQuantity = base + random.nextInt(range);
		return (int) (baseQuantity * intensityMultiplier);
	}
}