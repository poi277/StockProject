package Poi.Stock.features.Bot;

import java.util.List;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Websocket.StockCache;
import Poi.Stock.util.EnumUtil.BotType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DependsOn("webSocketService")
@RequiredArgsConstructor
public class BotInitializer {

	private final BotRepository botRepository;
	private final BotHaveStockRepository botHaveStockRepository;
	private final BotCache botCache;
	private final BotHaveStockCache botHaveStockCache;
	private final StockCache stockCache;

	private static final long INITIAL_ASSET = 100_000_000L;
	private static final int INITIAL_QUANTITY = 5000;

	@PostConstruct
	public void init() {
		initBots();
		initBotHaveStocks();
	}

	private void initBots() {
		createBot("BOT_MARKET_MAKER", BotType.MARKET_MAKER);
		createBot("BOT_RANDOM", BotType.RANDOM);
		createBot("BOT_TREND", BotType.TREND);
	}

	private void initBotHaveStocks() {
		botCache.getAll().forEach((botId, bot) -> {
			List<BotHaveStock> stocks = getOrCreateBotHaveStocks(bot, botId);
			botHaveStockCache.register(botId, stocks);
			log.info("봇 HaveStock 초기화 완료: {} / 종목 수: {}", botId, stocks.size());
		});
	}

	private void createBot(String botId, BotType botType) {
		Bot bot = getOrCreateBot(botId, botType);
		botCache.register(bot);
		log.info("봇 초기화 완료: {} / 자산: {}", botId, bot.getAsset());
	}

	private Bot getOrCreateBot(String botId, BotType botType) {
		return botRepository.findById(botId).orElseGet(() -> {
			Bot newBot = new Bot();
			newBot.setBotId(botId);
			newBot.setBotType(botType);
			newBot.setAsset(INITIAL_ASSET);
			return botRepository.save(newBot);
		});
	}

	private List<BotHaveStock> getOrCreateBotHaveStocks(Bot bot, String botId) {
		List<BotHaveStock> stocks = botHaveStockRepository.findByBot_BotId(botId);
		if (!stocks.isEmpty())
			return stocks;

		stocks = stockCache.getCache().keySet().stream().map(stockCode -> {
			BotHaveStock hs = new BotHaveStock();
			hs.setBot(bot);
			hs.setStockCode(stockCode);
			hs.setQuantity(INITIAL_QUANTITY);
			hs.setAveragePrice(stockCache.get(stockCode).getClosePrice());
			return hs;
		}).toList();

		return botHaveStockRepository.saveAll(stocks);
	}
}