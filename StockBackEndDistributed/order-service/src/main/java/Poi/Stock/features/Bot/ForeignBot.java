package Poi.Stock.features.Bot;

import java.util.Map;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.MarketState;
import Poi.Stock.util.EnumUtil.TradeDecision;

public class ForeignBot extends AbstractBot {

	public ForeignBot(String botId, int botBaseIntensity, BotOrderService botOrderService, BotCache botCache,
			BotStockCache botStockCache, BotService botService, MarketStateHolder marketStateHolder,
			BotHaveStockCache botHaveStockCache, CandleCacheService candleCacheService,
			AssignedCodeHolder assignedCodeHolder) {
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder, botHaveStockCache,
				candleCacheService, assignedCodeHolder, botId, botBaseIntensity);
	}
	@Override
	public BotType getBotType() {
		return BotType.FOREIGN;
	}
	@Override
	protected int getCancelHogaLevel() {
		return 15;
	}
	@Override
	protected int getMarketShockCancelProbability() {
		return 40;
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
	protected void executeTrade(StockRealTimeSnapshot stock, int currentPrice, MarketState state,
			int assetBonus, int finalIntensity) {
		String stockCode = stock.getStockCode();

		Map<Integer, Double> ma = getLatestMA(stockCode);
		double ma20 = ma.getOrDefault(20, 0.0);
		double ma60 = ma.getOrDefault(60, 0.0);
		double maMax = Math.max(ma20, ma60);

		switch (decideAction(currentPrice, ma20, ma60, state, assetBonus)) {
		case BUY -> executeBuy(stock, currentPrice, finalIntensity);
		case SELL -> executeSell(stock, currentPrice, maMax * 0.95, maMax * 0.97, maMax * 0.99, state,
				assetBonus, finalIntensity);
		case HOLD -> {
		}
		}
	}
	private TradeDecision decideAction(int currentPrice, double ma20, double ma60, MarketState state, int assetBonus) {
		int baseIntensity = getBotBaseIntensity();
		int roll = random.nextInt(100);

		// 각 행동별 가중치(Weight) 변수
		int buyWeight = 0;
		int holdWeight = 0;
		int sellWeight = 0;

		// 이평선 데이터가 없으면 횡보장 로직으로 처리
		MarketState effectiveState = (ma20 == 0.0 || ma60 == 0.0) ? MarketState.FLAT : state;

		switch (effectiveState) {
		case BULL: // 1. 상승장
			// 이평선 정배열 및 가격 위치에 따른 강도 분기
			int trendIntensity = 0;
			if (currentPrice > ma60 && ma20 > ma60) {
				trendIntensity = baseIntensity;
			} else if (currentPrice > ma20) {
				trendIntensity = (int) (baseIntensity * 0.66);
			} else if (currentPrice > ma60) {
				trendIntensity = baseIntensity / 3;
			}

			// 클수록 매수↑, 관망/매도 적절 / 작을수록 매도↑, 관망/매수 적절
			// 기존 코드의 'threshold * 0.7' 성향을 반영하여 매수 가중치 밸런싱
			buyWeight = (int) ((trendIntensity + assetBonus) * 0.7);
			sellWeight = Math.max(10, 120 - baseIntensity); // 전체 intensity가 작을수록 매도세 증가
			holdWeight = 30; // 관망 완충지대
			break;

		case FLAT:
			int flatBase = Math.max(10, baseIntensity - 20);
			buyWeight = (int) (flatBase * 0.5) + assetBonus;
			sellWeight = flatBase / 2;
			holdWeight = Math.max(10, 100 - baseIntensity);
			break;

		case BEAR:
			int bearBuyBase = baseIntensity / 6;
			buyWeight = Math.max(10, 120 - baseIntensity) + (int) ((bearBuyBase + assetBonus) * 0.4);
			sellWeight = baseIntensity;
			holdWeight = 30;
			break;

		default:
			return TradeDecision.HOLD;
		}
		int totalWeight = buyWeight + holdWeight + sellWeight;

		int buyThreshold = (buyWeight * 100) / totalWeight;
		int holdThreshold = buyThreshold + ((holdWeight * 100) / totalWeight);

		// 주사위 굴려 최종 결정
		if (roll < buyThreshold) {
			return TradeDecision.BUY;
		} else if (roll < holdThreshold) {
			return TradeDecision.HOLD;
		} else {
			return TradeDecision.SELL;
		}
	}
}