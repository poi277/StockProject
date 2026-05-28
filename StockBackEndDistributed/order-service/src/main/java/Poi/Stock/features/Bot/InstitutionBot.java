package Poi.Stock.features.Bot;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.util.EnumUtil.MarketState;
import Poi.Stock.util.EnumUtil.tradeType;

@Component
public class InstitutionBot extends AbstractBot {
	private final BotHaveStockCache botHaveStockCache;

	public InstitutionBot(BotOrderService botOrderService, BotCache botCache, BotStockCache botStockCache,
			BotService botService, MarketStateHolder marketStateHolder, BotHaveStockCache botHaveStockCache) {
		super(botOrderService, botCache, botStockCache, botService, marketStateHolder);
		this.botHaveStockCache = botHaveStockCache;
	}

	@Override
	protected String getBotId() {
		return "BOT_INSTITUTION";
	}

	@Scheduled(fixedDelay = 50000)
	@Override
	public void placeOrders() {
		super.placeOrders();
	}

	@Override
	protected void executeStrategy(boolean isBuy, Stock stock) {
		// 1. 현재 자산 및 상태 스냅샷 가져오기
		BotHaveStock haveStock = botHaveStockCache.get(getBotId(), stock.getStockCode());
		int currentQuantity = (haveStock != null) ? haveStock.getQuantity() : 0;
		double averagePrice = (haveStock != null) ? haveStock.getAveragePrice() : 0.0;

		int currentPrice = stock.getClosePrice();
		int tickSize = stock.getTickSize(currentPrice);
		int intensity = marketStateHolder.getIntensity();

		int targetQuantity = 10000;

		// 2. 목표 물량 달성 여부에 따른 매매 분기
		if (currentQuantity < targetQuantity) {
			// [매집] 미달성 시: 하락장(BEAR)에서 지정가 가이드라인에 맞춰 야금야금 매수
			if (marketStateHolder.getState() == MarketState.BEAR) {
				// 공포수치+차트에서의 최저가를 조합하여 price랑 quantity를 도출
				int price = currentPrice - tickSize;
				int quantity = calculateQuantity(500, 500, intensity); // 기본 500~999주 + 가중치

				if (botService.canBuy(getBotId(), price, quantity)) {
					botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(), tradeType.BUY,
							price, quantity);
				}
			}
		} else {
			// [익절] 달성 시: 상승장(BULL)에서 평단가 가이드라인 확인 후 야금야금 매도
			if (marketStateHolder.getState() == MarketState.BULL && averagePrice > 0) {

				// 익절 가격 가이드라인 계산
				int targetProfitPrice = calculateTargetProfitPrice(averagePrice, intensity);

				// 현재가가 익절 가이드라인을 넘었을 때만 실행
				if (currentPrice >= targetProfitPrice) {

					int price = currentPrice + tickSize;

					int quantity = calculateQuantity(200, 300, intensity); // 기본 200~499주 + 가중치

					// 오버 매도 방지 가이드라인 적용
					quantity = Math.min(quantity, currentQuantity);

					if (botService.canSell(getBotId(), stock.getStockCode(), quantity)) {
						botOrderService.placeOrder(getBotId(), stock.getStockCode(), stock.getStockName(),
								tradeType.SELL, price, quantity);
					}
				}
			}
		}
	}

	/**
	 * [가이드라인 함수 1] 시장 강도(intensity)를 반영한 동적 주문 수량 계산
	 */
	private int calculateQuantity(int base, int range, int intensity) {
		double intensityMultiplier = 1.0 + (intensity / 100.0);
		int baseQuantity = base + random.nextInt(range);
		return (int) (baseQuantity * intensityMultiplier);
	}

	/**
	 * [가이드라인 함수 2] 평단가 및 시장 강도(intensity)를 반영한 목표 익절가 계산
	 */
	private int calculateTargetProfitPrice(double averagePrice, int intensity) {
		// intensity가 높을수록 목표 익절%를 낮춰 1%~3% 사이로 동적 조절
		double targetProfitPercent = 0.03 - (0.02 * (intensity / 100.0));
		return (int) (averagePrice * (1.0 + targetProfitPercent));
	}
}