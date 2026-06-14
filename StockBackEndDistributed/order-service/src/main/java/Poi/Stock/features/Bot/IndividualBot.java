package Poi.Stock.features.Bot;


import java.util.Map;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import Poi.Stock.util.EnumUtil.MarketState;
import Poi.Stock.util.EnumUtil.TradeDecision;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IndividualBot extends AbstractBot {

	private final String botId;

	// 🎯 생성자 파라미터에 OrderCancelService 추가 및 super()에 전달
	public IndividualBot(String botId, BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache,
			BotService botService, MarketStateHolder marketStateHolder, BotHaveStockCache botHaveStockCache,
			CandleCacheService candleCacheService, AssignedCodeHolder assignedCodeHolder) {

		// 🎯 부모 생성자 규격(8개 인자)에 정확히 맞춰서 넘겨줍니다. 맨 뒤의 중복 botService 제거!
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder, botHaveStockCache,
				candleCacheService, assignedCodeHolder);

		this.botId = botId;
	}

	@Override
	protected String getBotId() {
		return this.botId;
	}

	@Override
	protected int getMarketShockCancelProbability() {
		return 80;
	}

	@Override
	protected int getCancelHogaLevel() {
		return 5; // 5호가창 이상 벌어지면 미련 없이 취소!
	}

	@Override
	public BotType getBotType() {
		return BotType.INDIVIDUAL;
	}

	@Override
	protected int getBotBaseIntensity() {
		return 60;
	}

	@Override
	protected int getBuyBase() {
		return 1;
	}

	@Override
	protected int getSellBase() {
		return 1;
	}

	@Override
	protected int getBuyRange() {
		return 10;
	}

	@Override
	protected int getSellRange() {
		return 10;
	}

	@Override
	public void placeOrders() {
		super.placeOrders();
	}

	@Override
	protected int calculateBuyPrice(int currentPrice, int tickSize, int finalIntensity) {
		return super.calculateBuyPrice(currentPrice, tickSize, finalIntensity);
	}

	@Override
	protected int calculateSellPrice(int currentPrice, int tickSize, int finalIntensity) {
		return super.calculateSellPrice(currentPrice, tickSize, finalIntensity);
	}

	@Override
	protected void executeTrade(StockRealTimeSnapshot stock, int currentPrice, int tickSize, MarketState state,
			int assetBonus, int finalIntensity) {
		String stockCode = stock.getStockCode();

		Map<Integer, Double> ma = getLatestMA(stockCode);
		double ma5 = ma.getOrDefault(5, 0.0);
		double ma20 = ma.getOrDefault(20, 0.0);
		double ma60 = ma.getOrDefault(60, 0.0);

		TradeDecision decision = decideAction(currentPrice, ma5, ma20, ma60, state, assetBonus);

		switch (decision) {
		case BUY -> executeBuy(stock, currentPrice, tickSize, finalIntensity);
		case SELL -> executeSell(stock, currentPrice, tickSize, ma5, ma20, ma60, state, assetBonus, finalIntensity);
		case HOLD -> log.debug("관망 - stockCode: {}", stockCode);
		}
	}

	private TradeDecision decideAction(int currentPrice, double ma5, double ma20, double ma60, MarketState state,
			int assetBonus) {
		int baseIntensity = getBotBaseIntensity();
		int roll = random.nextInt(100);

		// 각 행동별 가중치(Weight) 변수
		int buyWeight = 0;
		int holdWeight = 0;
		int sellWeight = 0;

		switch (state) {
		case BULL: // 1. 상승장
			// 클수록 매수↑, 관망/매도 적절히 / 작을수록 매도↑, 관망/매수 적절히
			buyWeight = baseIntensity + assetBonus; // intensity 비례 증가
			sellWeight = Math.max(10, 120 - baseIntensity); // intensity 반비례 (최소 10 보장)
			holdWeight = 30; // 완충 지대 (관망 확률 유지)
			break;

		case FLAT: // 2. 횡보장
			// 클수록 매수나 매도↑ / 작을수록 관망↑
			buyWeight = (baseIntensity / 2) + assetBonus;
			sellWeight = baseIntensity / 2;
			holdWeight = Math.max(10, 100 - baseIntensity); // intensity가 작을수록 관망 가중치 극대화
			break;

		case BEAR: // 3. 하락장
			// 클수록 매도↑, 관망/매수 적절히 / 작을수록 매수↑, 관망/매도 적절히
			sellWeight = baseIntensity;
			buyWeight = Math.max(10, 120 - baseIntensity) + assetBonus;
			holdWeight = 30;
			break;

		default:
			return TradeDecision.HOLD;
	    }

		// --- 🎯 가중치를 기반으로 확률 구간(정규화) 계산 ---
		int totalWeight = buyWeight + holdWeight + sellWeight;

		// 0 ~ 100% 비율로 변환
		int buyThreshold = (buyWeight * 100) / totalWeight;
		int holdThreshold = buyThreshold + ((holdWeight * 100) / totalWeight);

		// 주사위 굴려 결정
		if (roll < buyThreshold) {
			return TradeDecision.BUY;
		} else if (roll < holdThreshold) {
			return TradeDecision.HOLD;
		} else {
			return TradeDecision.SELL;
		}
	}
}