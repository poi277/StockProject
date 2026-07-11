package Poi.Stock.features.Bot;

import java.util.List;
import java.util.Map;
import java.util.Random;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleWithMA;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.CandleType;
import Poi.Stock.util.EnumUtil.MarketState;
import Poi.Stock.util.EnumUtil.tradeType;
import Poi.Stock.util.TickSizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBot {

	public abstract BotType getBotType();

	protected final BotOrderService botOrderService;
    protected final BotCache botCache;
    protected final BotStockCache botStockCache;
    protected final BotService botService;
    protected final MarketStateHolder marketStateHolder;
    protected final BotHaveStockCache botHaveStockCache;
    protected final CandleCacheService candleCacheService;
    protected final AssignedCodeHolder assignedCodeHolder;
	protected final String botId;
	protected final int botBaseIntensity;

	protected final Random random = new Random();

	protected abstract void executeTrade(StockRealTimeSnapshot stock, int currentPrice, MarketState state,
			int assetBonus, int finalIntensity);
	protected abstract int getBuyBase();
	protected abstract int getBuyRange();
	protected abstract int getSellBase();
	protected abstract int getSellRange();

	protected abstract int getCancelHogaLevel();

	protected abstract int getMarketShockCancelProbability();

	public String getBotId() {
		return this.botId;
	}
	public int getBotBaseIntensity() {
		return this.botBaseIntensity;
	}

	public void placeOrders() {
		String botId = getBotId();
		BotType type = this.getBotType();

		Bot bot = botCache.get(botId);
		if (bot == null)
			return;
		// log.info("[주문 시작] 봇 ID: {} / 종류: {} / 현재 자산: {}", botId, type,
		// bot.getAsset());

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

		MarketState state = marketStateHolder.getState(stockCode);
		Bot bot = botCache.get(getBotId());
		int assetBonus = (int) Math.min(30, bot.getAsset() / 100_000);
		int finalIntensity = getFinalIntensity(stockCode);

		executeTrade(stock, currentPrice, state, assetBonus, finalIntensity);
	}

	protected int getFinalIntensity(String stockCode) {
		int marketIntensity = marketStateHolder.getIntensity(stockCode);
		int botIntensity = getBotBaseIntensity();
		return (marketIntensity + botIntensity) / 2;
	}

	protected Map<Integer, Double> getLatestMA(String stockCode) {
		List<CandleWithMA<Candle>> candles = candleCacheService.getCacheCandles(CandleType.ONE_MINUTE, stockCode);
		if (candles == null || candles.isEmpty())
			return Map.of();
		Map<Integer, Double> ma = candles.get(candles.size() - 1).getMa();
		return ma != null ? ma : Map.of();
	}

	// 사는 가격 계산 (호가 단위 방어)
	protected int calculateBuyPrice(int currentPrice, int finalIntensity) {
	    int maxDiscountTicks = (100 - finalIntensity + random.nextInt(100)) / 10;

	    if (maxDiscountTicks <= 0)
	        maxDiscountTicks = 1;

	    int randomTickCount = random.nextInt(maxDiscountTicks);

	    // 현재가에서 한 틱 위로 보정 후, randomTickCount만큼 아래로 이동
	    int basePrice = TickSizeUtil.addTicks(currentPrice, 1);
	    int targetPrice = TickSizeUtil.subtractTicks(basePrice, randomTickCount);

	    return Math.max(TickSizeUtil.getTickSize(1), targetPrice);
	}

	protected int calculateSellPrice(int currentPrice, int finalIntensity) {
	    int maxPremiumTicks = (100 - finalIntensity + random.nextInt(100)) / 10;
	    if (maxPremiumTicks <= 0)
	        maxPremiumTicks = 1;

	    int randomTickCount = random.nextInt(maxPremiumTicks);

	    // 현재가에서 한 틱 아래로 보정 후, randomTickCount만큼 위로 이동
	    int basePrice = TickSizeUtil.subtractTicks(currentPrice, 1);
	    return TickSizeUtil.addTicks(basePrice, randomTickCount);
	}

	protected void executeBuy(StockRealTimeSnapshot stock, int currentPrice, int finalIntensity) {
		String stockCode = stock.getStockCode();
		int quantity = calculateQuantity(getBuyBase(), getBuyRange(), finalIntensity);
		int price = calculateBuyPrice(currentPrice, finalIntensity);
		if (botService.canBuy(getBotId(), price, quantity)) {
			botOrderService.placeOrder(getBotId(), stockCode, stock.getStockName(), tradeType.BUY, price, quantity);
		}
	}

	protected void executeSell(StockRealTimeSnapshot stock, int currentPrice, double maLow, double maMid,
			double maHigh, MarketState state, int assetBonus, int finalIntensity) {
		String stockCode = stock.getStockCode();
		BotHaveStock haveStock = botHaveStockCache.get(getBotId(), stockCode);
		if (haveStock == null || haveStock.getQuantity() <= 0)
			return;
		int quantity = calculateQuantity(getSellBase(), getSellRange(), finalIntensity);
		quantity = Math.min(quantity, haveStock.getQuantity());
		int price = calculateSellPrice(currentPrice, finalIntensity);

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