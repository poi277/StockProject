package Poi.Stock.features.Bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import Poi.Stock.features.Order.Order;
import Poi.Stock.features.Order.OrderBook;
import Poi.Stock.features.Order.OrderBookCache;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.object.MatchingResult;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.util.EnumUtil.MarketState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService {

	private final BotCache botCache;
	private final BotStockCache botStockCache;
	private final BotHaveStockCache botHaveStockCache;
	private final OrderRepository orderRepository;
	private final OrderBookCache orderBookCache;

	// 봇 자산 변경
	public void updateAsset(String botId, long delta) {
		Bot bot = botCache.get(botId);
		if (bot == null)
			return;
		bot.setAsset(bot.getAsset() + delta);
	}

	// 봇 보유 주식 수량 변경
	public void updateStock(String botId, String stockCode, int delta) {
		BotHaveStock hs = botHaveStockCache.get(botId, stockCode);
		if (hs == null)
			return;
		hs.setQuantity(hs.getQuantity() + delta);
	}

	// 봇 자산 조회
	public long getAsset(String botId) {
		Bot bot = botCache.get(botId);
		if (bot == null)
			return 0;
		return bot.getAsset();
	}

	// 봇 보유 주식 수량 조회
	public int getStockQuantity(String botId, String stockCode) {
		BotHaveStock hs = botHaveStockCache.get(botId, stockCode);
		if (hs == null)
			return 0;
		return hs.getQuantity();
	}

	// 봇 매수 가능 여부 (자산 체크)
	public boolean canBuy(String botId, int price, int quantity) {
		return getAsset(botId) >= (long) price * quantity;
	}

	// 봇 매도 가능 여부 (주식 수량 체크)
	public boolean canSell(String botId, String stockCode, int quantity) {
		return getStockQuantity(botId, stockCode) >= quantity;
	}

	public void setBotStockCache(MatchingResult result) {
		if (result == null || result.getExecutions().isEmpty()) {
			return;
		}
		StockRealTimeSnapshot snapshot = botStockCache.get(result.getStockCode());
		if (snapshot == null) {
			return;
		}

		Integer lastPrice = result.getLastExecutionPrice();
		Integer maxPrice = result.getMaxExecutionPrice();
		Integer minPrice = result.getMinExecutionPrice();

		snapshot.setCurrentPrice(lastPrice);

		// 고가 업데이트
		if (maxPrice > snapshot.getHighPrice()) {
			snapshot.setHighPrice(maxPrice);
		}

		// 저가 업데이트
		if (minPrice < snapshot.getLowPrice()) {
			snapshot.setLowPrice(minPrice);
		}

		// 3. 등락폭(changeAmount)과 등락률(changeRate)도 실시간으로 봇이 인지할 수 있게 연산 추가
		int yesterdayClose = snapshot.getYesterdayClosePrice();
		int changeAmount = lastPrice - yesterdayClose;
		double changeRate = yesterdayClose != 0 ? ((double) changeAmount / yesterdayClose) * 100.0 : 0.0;

		snapshot.setChangeAmount(changeAmount);
		snapshot.setChangeRate(changeRate);

		// 4. 복사본을 만들어 멀티스레드 환경이나 안전한 봇 매매 판단을 위해 캐시에 갱신
		// (만약 botStockCache 내부 맵이 객체 주소를 직접 공유해도 상관없는 가벼운 구조라면 copy 없이 그냥 둬도 무방합니다)
		botStockCache.put(result.getStockCode(), snapshot);
	}

	public void cancelBotOrdersByMarketShock(String botId, String stockCode, MarketState currentState,
			int cancelProbability) {
		OrderBook book = orderBookCache.get(stockCode);
		int realProbability = (int) Math.max(1, cancelProbability * 0.05);
		Random random = new Random();
		if (book == null || random.nextInt(100) > realProbability)
			return;

		List<Order> toCancel = new ArrayList<>();
		if (currentState == MarketState.BEAR) {
			book.getBuyBook().values().stream().flatMap(level -> level.getOrders().stream())
					.filter(o -> o.getUserId().equals(botId)).forEach(o -> {
						toCancel.add(o);
					});
		} else if (currentState == MarketState.BULL) {
			book.getSellBook().values().stream().flatMap(level -> level.getOrders().stream())
					.filter(o -> o.getUserId().equals(botId)).forEach(o -> {
						toCancel.add(o);
					});
		}

		if (toCancel.isEmpty())
			return;
		toCancel.forEach(book::removeOrder);
		List<Order> dbOrders = toCancel.stream().filter(o -> o.getOrderId() != null).toList();

		if (!dbOrders.isEmpty()) {
			orderRepository.deleteAllInBatch(dbOrders);
		}

		log.info("[시장급변 미세취소] 봇: {} / 종목: {} / 장세: {} / 적용확률: {}% / {}건 취소 (DB 삭제: {}건)", botId, stockCode,
				currentState, realProbability, toCancel.size(), dbOrders.size());
	}

	public void cancelOutOfRange(String botId, String stockCode, int currentPrice, int tickSize, int hogaLevel) {
		OrderBook book = orderBookCache.get(stockCode);
		if (book == null)
			return;
		int range = tickSize * hogaLevel;
		List<Order> toCancel = Stream
				.concat(book.getSellBook().values().stream().flatMap(level -> level.getOrders().stream()),
						book.getBuyBook().values().stream().flatMap(level -> level.getOrders().stream()))
				.filter(order -> order.getUserId().equals(botId))
				.filter(order -> Math.abs(order.getTradePrice() - currentPrice) > range).toList();

		if (toCancel.isEmpty())
			return;
		toCancel.forEach(book::removeOrder);

		// 🎯 2. 수정 포인트: orderId가 null이 아닌(DB에 존재하는) 주문만 골라내서 이괄 삭제
		List<Order> dbOrders = toCancel.stream().filter(o -> o.getOrderId() != null).toList();

		if (!dbOrders.isEmpty()) {
			orderRepository.deleteAllInBatch(dbOrders);
		}

		log.info("[호가이탈 취소] 봇: {} / 종목: {} / 현재가: {} / {}건 취소 완료 (DB 삭제: {}건)", botId, stockCode, currentPrice,
				toCancel.size(), dbOrders.size());
	}
}