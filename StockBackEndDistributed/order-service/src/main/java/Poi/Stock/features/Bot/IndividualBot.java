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
public class IndividualBot extends AbstractBot {

	public IndividualBot(BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache,
			BotService botService, MarketStateHolder marketStateHolder, BotHaveStockCache botHaveStockCache,
			CandleCacheService candleCacheService, AssignedCodeHolder assignedCodeHolder) {
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder, botHaveStockCache,
				candleCacheService, assignedCodeHolder);
    }

	@Override
	protected String getBotId() {
		return "BOT_INDIVIDUAL";
	}

    @Override
	protected int getBotBaseIntensity() {
		// 이 봇은 개인적인 성향이 80
		return 80;
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

		// 공통 데이터 준비
		double ma5 = 0.0, ma20 = 0.0, ma60 = 0.0;
		List<CandleWithMA<CandleMinute>> candles = candleCacheService.getCandles(CandleType.ONE_MINUTE, stockCode);
		if (candles != null && !candles.isEmpty()) {
			Map<Integer, Double> ma = candles.get(candles.size() - 1).getMa();
			if (ma != null) {
				ma5 = ma.getOrDefault(5, 0.0);
				ma20 = ma.getOrDefault(20, 0.0);
				ma60 = ma.getOrDefault(60, 0.0);
			}
		}

		MarketState state = marketStateHolder.getState(stockCode);
		Bot bot = botCache.get(getBotId());
		int assetBonus = (int) Math.min(30, bot.getAsset() / 1_000_000);
		int finalIntensity = getFinalIntensity(stockCode);

		if (shouldBuy(currentPrice, ma5, ma20, ma60, state, assetBonus)) {
			executeBuy(stock, currentPrice, tickSize);
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
		// 1.돈이 많을수록 매수
		// 2.이평선이 깨질수록 매수
		// 확률에 의거하거 매도 할 수도 있음
		return (buyProb > 0) && (random.nextInt(100) < (buyProb + assetBonus));
	}

	private void executeBuy(StockRealTimeSnapshot stock, int currentPrice, int tickSize) {
		String stockCode = stock.getStockCode();
		int quantity = 1 + random.nextInt(10);
		int price = currentPrice + tickSize - (tickSize * random.nextInt(marketStateHolder.peoplevix(stockCode)));
		if (botService.canBuy(getBotId(), price, quantity)) {
			botOrderService.placeOrder(getBotId(), stockCode, stock.getStockName(), tradeType.BUY, price, quantity);
		}
	}

	private void executeSell(StockRealTimeSnapshot stock, int currentPrice, int tickSize, double ma5, double ma20,
			double ma60, MarketState state, int assetBonus, int finalIntensity) {
		String stockCode = stock.getStockCode();
		BotHaveStock haveStock = botHaveStockCache.get(getBotId(), stockCode);
		if (haveStock == null || haveStock.getQuantity() <= 0)
			return;

		// 수익률 계산
		double avgPrice = haveStock.getAveragePrice();
		double profitRate = (currentPrice - avgPrice) / avgPrice * 100;

		int sellProb = 0;
		if (state == MarketState.FLAT) {
			// 횡보장일때 30%확률로 팜
			sellProb = 30;
		} else if (state == MarketState.BULL) {
			// 상승장일떄 수익률이 날수록 팔확률이 올라감
			if (currentPrice > ma60)
				sellProb = 60 + (int) Math.min(30, profitRate * 5);
			else if (currentPrice > ma20)
				sellProb = 40 + (int) Math.min(20, profitRate * 3);
			else if (currentPrice > ma5)
				sellProb = 20 + (int) Math.min(10, profitRate * 2);
        } else {
			// 하락장일때 손실률이 클수록 강하게 매도
			// 수익률을 리버스하여 수익이 많이 나도 강하게 매도
			double lossRate = -profitRate;
			if (currentPrice > ma60)
				sellProb = 60 + (int) Math.min(30, lossRate * 5);
			else if (currentPrice > ma20)
				sellProb = 40 + (int) Math.min(20, lossRate * 3);
			else if (currentPrice > ma5)
				sellProb = 20 + (int) Math.min(10, lossRate * 2);
		}

		sellProb = (int) (sellProb * (finalIntensity / 100.0));

		if (sellProb > 0 && random.nextInt(100) < sellProb + assetBonus) {
			int quantity = 1 + random.nextInt(10);
			int price = currentPrice - tickSize + (tickSize * random.nextInt(marketStateHolder.peoplevix(stockCode)));
			if (price > 0 && botService.canSell(getBotId(), stockCode, quantity)) {
				botOrderService.placeOrder(getBotId(), stockCode, stock.getStockName(), tradeType.SELL, price,
						quantity);
            }
        }
    }
}