package Poi.Stock.features.Bot;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService {

	private final BotCache botCache;
	private final BotHaveStockCache botHaveStockCache;

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
}