package Poi.Stock.features.Bot;

import java.util.List;
import java.util.Map;
import java.util.Random;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.CandleType;
import Poi.Stock.util.EnumUtil.MarketState;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	public abstract BotType getBotType();
    protected abstract int getBotBaseIntensity();

	protected abstract void executeTrade(StockRealTimeSnapshot stock, int currentPrice, int tickSize, MarketState state,
			int assetBonus, int finalIntensity);
	protected abstract int getBuyBase();
	protected abstract int getBuyRange();
	protected abstract int getSellBase();
	protected abstract int getSellRange();

	protected abstract int getCancelHogaLevel(); // 현재가와 호가 벌어지면 취소

	protected abstract int getMarketShockCancelProbability(); // 취소 처리될 확률 (%)

	public void placeOrders() {
		String botId = getBotId();
		BotType type = this.getBotType();

		Bot bot = botCache.get(botId);
		if (bot == null)
			return;
		log.info("[주문 시작] 봇 ID: {} / 종류: {} / 현재 자산: {}", botId, type, bot.getAsset());

		assignedCodeHolder.getAssignedCodes().forEach(stockCode -> {
			StockRealTimeSnapshot stock = botStockCache.get(stockCode);
			if (stock == null || stock.getCurrentPrice() <= 0)
				return;
			executeCancelStrategy(stock);
			executeStrategy(stock);
		});
	}

	protected void executeStrategy(StockRealTimeSnapshot stock) {
		String stockCode = stock.getStockCode();
		int currentPrice = stock.getCurrentPrice();
		int tickSize = stock.getTickSize(currentPrice);

		MarketState state = marketStateHolder.getState(stockCode);
		Bot bot = botCache.get(getBotId());
		int assetBonus = (int) Math.min(30, bot.getAsset() / 100_000);
		int finalIntensity = getFinalIntensity(stockCode);

		executeTrade(stock, currentPrice, tickSize, state, assetBonus, finalIntensity);
	}

	protected int getFinalIntensity(String stockCode) {
		int marketIntensity = marketStateHolder.getIntensity(stockCode);
		int botIntensity = getBotBaseIntensity();
		return (marketIntensity + botIntensity) / 2;
	}

	protected Map<Integer, Double> getLatestMA(String stockCode) {
		List<CandleWithMA<CandleMinute>> candles = candleCacheService.getCandles(CandleType.ONE_MINUTE, stockCode);
		if (candles == null || candles.isEmpty())
			return Map.of();
		Map<Integer, Double> ma = candles.get(candles.size() - 1).getMa();
		return ma != null ? ma : Map.of();
	}

	// 사는 가격 계산 (호가 단위 방어)
	protected int calculateBuyPrice(int currentPrice, int tickSize, int finalIntensity) {
	    int maxDiscountTicks = (100 - finalIntensity + random.nextInt(100)) / 10;
	    
	    if (maxDiscountTicks <= 0)
	        maxDiscountTicks = 1;
	        
	    int randomTickCount = random.nextInt(maxDiscountTicks);

		int targetPrice = currentPrice + tickSize - (randomTickCount * tickSize);
		return Math.max(tickSize, targetPrice);
	}
	protected int calculateSellPrice(int currentPrice, int tickSize, int finalIntensity) {
		int maxPremiumTicks = (100 - finalIntensity + random.nextInt(100)) / 10;
		if (maxPremiumTicks <= 0)
			maxPremiumTicks = 1;
		int randomTickCount = random.nextInt(maxPremiumTicks);
		return currentPrice - tickSize + (randomTickCount * tickSize);
	}

	protected void executeBuy(StockRealTimeSnapshot stock, int currentPrice, int tickSize, int finalIntensity) {
		String stockCode = stock.getStockCode();
		int quantity = calculateQuantity(getBuyBase(), getBuyRange(), finalIntensity);
		int price = calculateBuyPrice(currentPrice, tickSize, finalIntensity);
		if (botService.canBuy(getBotId(), price, quantity)) {
			botOrderService.placeOrder(getBotId(), stockCode, stock.getStockName(), tradeType.BUY, price, quantity);
		}
	}
	protected void executeSell(StockRealTimeSnapshot stock, int currentPrice, int tickSize, double maLow, double maMid,
			double maHigh, MarketState state, int assetBonus, int finalIntensity) {
		String stockCode = stock.getStockCode();
		BotHaveStock haveStock = botHaveStockCache.get(getBotId(), stockCode);
		if (haveStock == null || haveStock.getQuantity() <= 0)
			return;
		int quantity = calculateQuantity(getSellBase(), getSellRange(), finalIntensity);
		quantity = Math.min(quantity, haveStock.getQuantity());
		int price = calculateSellPrice(currentPrice, tickSize, finalIntensity);

		if (botService.canSell(getBotId(), stockCode, quantity)) {
			botOrderService.placeOrder(getBotId(), stockCode, stock.getStockName(), tradeType.SELL, price, quantity);
		}
	}

	protected int calculateSellProb(int currentPrice, double maLow, double maMid, double maHigh, MarketState state,
			double profitRate) {
		if (state == MarketState.FLAT)
			return 30;

		double rate = (state == MarketState.BULL) ? profitRate : -profitRate;

		if (currentPrice > maHigh)
			return 60 + (int) Math.min(30, rate * 5);
		else if (currentPrice > maMid)
			return 40 + (int) Math.min(20, rate * 3);
		else if (currentPrice > maLow)
			return 20 + (int) Math.min(10, rate * 2);

		return 0;
	}

	protected int calculateQuantity(int base, int range, int finalIntensity) {
		double intensityMultiplier = 1.0 + (finalIntensity / 100.0);
		int baseQuantity = base + random.nextInt(range);
		return (int) (baseQuantity * intensityMultiplier);
	}

	protected void executeCancelStrategy(StockRealTimeSnapshot stock) {
		String botId = getBotId();
		String stockCode = stock.getStockCode();
		int currentPrice = stock.getCurrentPrice();
		int tickSize = stock.getTickSize(currentPrice);
		MarketState currentState = marketStateHolder.getState(stockCode);

		botService.cancelOutOfRange(botId, stockCode, currentPrice, tickSize, getCancelHogaLevel());

		botService.cancelBotOrdersByMarketShock(botId, stockCode, currentState, getMarketShockCancelProbability());
	}
}