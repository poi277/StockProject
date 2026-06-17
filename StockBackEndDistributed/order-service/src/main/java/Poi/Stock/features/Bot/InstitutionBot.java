package Poi.Stock.features.Bot;

import java.util.Map;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.MarketState;
import Poi.Stock.util.EnumUtil.TradeDecision;

public class InstitutionBot extends AbstractBot {

	public InstitutionBot(String botId, int botBaseIntensity, BotOrderService botOrderService, BotCache botCache,
			BotStockCache botStockCache, BotService botService, MarketStateHolder marketStateHolder,
			BotHaveStockCache botHaveStockCache, CandleCacheService candleCacheService,
			AssignedCodeHolder assignedCodeHolder) {
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder, botHaveStockCache,
				candleCacheService, assignedCodeHolder, botId, botBaseIntensity);
	}
	@Override
	public BotType getBotType() {
		return BotType.INSTITUTION;
	}

	// 🎯 기관 봇의 취소 기준 호가창 설정
	@Override
	protected int getCancelHogaLevel() {
		return 30; // 30호가 이상 아주 멀리 벌어져야 "이제 취소할까" 하고 묵직하게 움직임
	}

	// 🎯 기관 봇의 시장 급변 시 취소 지수 설정
	@Override
	protected int getMarketShockCancelProbability() {
		return 10; // 폭락장이 와도 10 수준만 유지 (내부 보정 시 0.5% 미만의 확률로, 거의 안 빼고 벽을 지킴)
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
	protected int calculateBuyPrice(int currentPrice, int finalIntensity) {
		return super.calculateBuyPrice(currentPrice, finalIntensity);
	}

	@Override
	protected int calculateSellPrice(int currentPrice, int finalIntensity) {
		return super.calculateSellPrice(currentPrice, finalIntensity);
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

		// 예외 처리: 이평선 데이터가 없으면 안전하게 횡보장(FLAT) 로직으로 처리
		MarketState effectiveState = (ma20 == 0.0 || ma60 == 0.0) ? MarketState.FLAT : state;

		switch (effectiveState) {
		case BULL:
			if (currentPrice > ma60 && ma20 > ma60) {
				// 완벽한 상승 추세 (정배열) -> 매수 가중치 극대화
				buyWeight = (int) (baseIntensity * 1.2) + assetBonus;
				sellWeight = Math.max(10, 100 - baseIntensity);
			} else if (currentPrice > ma20) {
				// 준수한 상승 추세
				buyWeight = baseIntensity + assetBonus;
				sellWeight = Math.max(10, 120 - baseIntensity);
			} else {
				// 상승장이지만 일시적 눌림목 혹은 약세
				buyWeight = (int) (baseIntensity * 0.7) + assetBonus;
				sellWeight = Math.max(15, 130 - baseIntensity);
			}
			holdWeight = 30; // 관망 확률 완충 지대
			break;

		case FLAT: // 2. 횡보장 (이평선 기준 모호할 때 포함)
			// [기본 규칙] 클수록 매수/매도↑ (거래 활성화) / 작을수록 관망↑
			buyWeight = (baseIntensity / 2) + assetBonus;
			sellWeight = baseIntensity / 2;
			// baseIntensity가 작을수록 holdWeight가 커져서 관망 확률이 지배적이 됨
			holdWeight = Math.max(10, 100 - baseIntensity);
			break;

		case BEAR:
			if (currentPrice < ma60 && ma20 < ma60) {
				sellWeight = (int) (baseIntensity * 1.2);
				buyWeight = Math.max(10, 100 - baseIntensity) + assetBonus;
			} else {
				sellWeight = baseIntensity;
				buyWeight = Math.max(10, 120 - baseIntensity) + assetBonus;
			}
			holdWeight = 30;
			break;

		default:
			return TradeDecision.HOLD;
		}

		// --- 🎯 가중치 기반 확률 구간(0 ~ 100%) 정규화 계산 ---
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