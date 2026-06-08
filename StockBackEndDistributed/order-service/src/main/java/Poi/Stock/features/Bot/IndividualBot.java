package Poi.Stock.features.Bot;

import java.util.Map;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
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
    public void placeOrders() {
        super.placeOrders();
    }

    @Override
	protected void executeStrategy(boolean isBuy, StockRealTimeSnapshot stock, Map<Integer, Double> ma) {
		int currentPrice = stock.getCurrentPrice();
        int tickSize = stock.getTickSize(currentPrice);
		String stockCode = stock.getStockCode();

		double ma5 = ma.getOrDefault(5, 0.0);
		double ma20 = ma.getOrDefault(20, 0.0);
		double ma60 = ma.getOrDefault(60, 0.0);

		Bot bot = botCache.get(getBotId());
		long asset = bot.getAsset();

		// 자산 보너스 (100만원당 1%, 최대 30%)
		int assetBonus = (int) Math.min(30, asset / 1_000_000);

		// MA 단계별 기본 확률
		int baseProb = 0;
		if (isBuy) {
			if (currentPrice < ma60)
				baseProb = 60;
			else if (currentPrice < ma20)
				baseProb = 40;
			else if (currentPrice < ma5)
				baseProb = 20;
		} else {
			MarketState state = marketStateHolder.getState(stockCode);
			BotHaveStock haveStock = botHaveStockCache.get(getBotId(), stockCode);

			if (haveStock != null && haveStock.getQuantity() > 0) {
				double avgPrice = haveStock.getAveragePrice();
				double profitRate = (currentPrice - avgPrice) / avgPrice * 100;

				if (state == MarketState.BULL) {
					// 상승장: 수익률 높을수록 강하게 매도
					if (currentPrice > ma60)
						baseProb = 60 + (int) Math.min(30, profitRate * 5);
					else if (currentPrice > ma20)
						baseProb = 40 + (int) Math.min(20, profitRate * 3);
					else if (currentPrice > ma5)
						baseProb = 20 + (int) Math.min(10, profitRate * 2);
				} else {
					// 하락장: 손실률 높을수록 강하게 매도 (손절)
					double lossRate = -profitRate;
					if (currentPrice > ma60)
						baseProb = 60 + (int) Math.min(30, lossRate * 5);
					else if (currentPrice > ma20)
						baseProb = 40 + (int) Math.min(20, lossRate * 3);
					else if (currentPrice > ma5)
						baseProb = 20 + (int) Math.min(10, lossRate * 2);
				}
			}
		}

		int finalProb = baseProb + assetBonus;
		if (random.nextInt(100) >= finalProb)
			return;

		int quantity = 1 + random.nextInt(10);

        if (isBuy) {
			int price = currentPrice + tickSize - (tickSize * random.nextInt(marketStateHolder.peoplevix(stockCode)));
            if (botService.canBuy(getBotId(), price, quantity)) {
                botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(), tradeType.BUY, price, quantity);
            }
        } else {
			int price = currentPrice - tickSize + (tickSize * random.nextInt(marketStateHolder.peoplevix(stockCode)));
            if (price > 0 && botService.canSell(getBotId(), stock.getStockCode(), quantity)) {
                botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(), tradeType.SELL, price, quantity);
            }
        }
    }
}