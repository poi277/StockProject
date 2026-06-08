package Poi.Stock.features.Bot;

import java.util.Map;

import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
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
	public void placeOrders() {
		super.placeOrders();
	}

	@Override
	protected void executeStrategy(boolean isBuy, StockRealTimeSnapshot stock, Map<Integer, Double> ma) {
		BotHaveStock haveStock = botHaveStockCache.get(getBotId(), stock.getStockCode());
		int currentQuantity = (haveStock != null) ? haveStock.getQuantity() : 0;
		double averagePrice = (haveStock != null) ? haveStock.getAveragePrice() : 0.0;
		int currentPrice = stock.getCurrentPrice();
		int tickSize = stock.getTickSize(currentPrice);
		String stockCode = stock.getStockCode();
		int intensity = marketStateHolder.getIntensity(stockCode);

		double ma20 = ma.getOrDefault(20, 0.0);
		double ma60 = ma.getOrDefault(60, 0.0);
		double maMin = Math.min(ma20, ma60);

		int targetQuantity = 10000;

		if (currentQuantity < targetQuantity) {
			// MA20, MA60 최저가 근처일 때 매수
			if (ma20 > 0 && ma60 > 0 && currentPrice <= maMin * 1.01) {
				int price = currentPrice - tickSize;
				int quantity = calculateQuantity(500, 500, intensity);
				if (botService.canBuy(getBotId(), price, quantity)) {
					botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(), tradeType.BUY,
							price, quantity);
				}
			}
		} else {
			if (marketStateHolder.getState(stockCode) == MarketState.BULL && averagePrice > 0) {
				int targetProfitPrice = calculateTargetProfitPrice(averagePrice, intensity);
				if (currentPrice >= targetProfitPrice) {
					int price = currentPrice + tickSize;
					int quantity = calculateQuantity(200, 300, intensity);
					quantity = Math.min(quantity, currentQuantity);
					if (botService.canSell(getBotId(), stock.getStockCode(), quantity)) {
						botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(),
								tradeType.SELL, price, quantity);
					}
				}
			}
		}
	}

	private int calculateQuantity(int base, int range, int intensity) {
		double intensityMultiplier = 1.0 + (intensity / 100.0);
		int baseQuantity = base + random.nextInt(range);
		return (int) (baseQuantity * intensityMultiplier);
	}

	private int calculateTargetProfitPrice(double averagePrice, int intensity) {
		double targetProfitPercent = 0.03 - (0.02 * (intensity / 100.0));
		return (int) (averagePrice * (1.0 + targetProfitPercent));
	}
}