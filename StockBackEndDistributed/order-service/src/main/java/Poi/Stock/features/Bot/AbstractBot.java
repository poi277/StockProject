package Poi.Stock.features.Bot;

import java.util.Random;

import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.EnumUtil.MarketState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractBot {

	protected final BotOrderService botOrderService;
	protected final BotCache botCache;
	protected final BotStockCache botStockCache;
	protected final BotService botService;
	protected final MarketStateHolder marketStateHolder;

	protected final Random random = new Random();

	protected abstract String getBotId();

	// 매매 전략을 자식들이 구현하도록 이름 통일 (isBuy 가이드라인과 주식 정보를 넘김)
	protected abstract void executeStrategy(boolean isBuy, StockRealTimeSnapshot stock);

	public void placeOrders() {
		String botId = getBotId();
		Bot bot = botCache.get(botId);
		if (bot == null)
			return;

		StockRealTimeSnapshot stock = botStockCache.get("035420");
		if (stock == null || stock.getYesterdayClosePrice() <= 0)
			return;

		MarketState state = marketStateHolder.getState();
		boolean isBuy = false;

		// 시장 흐름에 따른 베이스 확률 연산
		if (state == MarketState.BULL) {
			isBuy = random.nextInt(100) <= marketStateHolder.getIntensity();
		} else if (state == MarketState.BEAR) {
			isBuy = random.nextInt(100) > marketStateHolder.getIntensity();
		}

		// 최종적으로 자식들의 고유 전략 메서드 실행
		executeStrategy(isBuy, stock);
	}
}