package Poi.Stock.features.Bot;

import java.util.List;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Stock.StockCache;
import Poi.Stock.features.Stock.StockRealTimeSnapshot;
import Poi.Stock.util.AssignedCodeHolder;
import Poi.Stock.util.EnumUtil.BotType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DependsOn({ "candleInit" })
@RequiredArgsConstructor
public class BotInit {

	private final BotRepository botRepository;
	private final BotHaveStockRepository botHaveStockRepository;
	private final BotCache botCache;
	private final BotStockCache botStockCache;
	private final BotHaveStockCache botHaveStockCache;
	private final StockCache stockCache;
	private final MarketStateHolder marketStateHolder;

	// 🎯 봇 객체를 동적으로 생성하기 위해 필요한 하위 서비스 의존성들을 주입받습니다.
	private final BotOrderService botOrderService;
	private final BotService botService;
	private final CandleCacheService candleCacheService;
	private final AssignedCodeHolder assignedCodeHolder;


	private static final long INITIAL_ASSET = 100_000_000L;
	private static final int INITIAL_QUANTITY = 5000;

	@PostConstruct
	public void init() {
		initBots();
		initBotHaveStocks();
		initBotCache();
	}

	private void initBots() {
		for (BotList botInfo : BotList.values()) {
			String botId = botInfo.getBotId();
			BotType botType = botInfo.getBotType();
			int intensity = botInfo.getBotBaseIntensity();
			Bot bot = getOrCreateBot(botId, botType);
			botCache.register(bot);
			AbstractBot botInstance = null;
			if (botType == BotType.INDIVIDUAL) {
				botInstance = new IndividualBot(botId, intensity, botOrderService, botCache, botStockCache, botService,
						marketStateHolder, botHaveStockCache, candleCacheService, assignedCodeHolder);
			} else if (botType == BotType.INSTITUTION) {
				botInstance = new InstitutionBot(botId, intensity, botOrderService, botCache, botStockCache, botService,
						marketStateHolder, botHaveStockCache, candleCacheService, assignedCodeHolder);
			} else if (botType == BotType.FOREIGN) {
				botInstance = new ForeignBot(botId, intensity, botOrderService, botCache, botStockCache, botService,
						marketStateHolder, botHaveStockCache, candleCacheService, assignedCodeHolder);
			}

			if (botInstance != null) {
				botCache.registerInstance(botInstance);
			}

			log.info("봇 초기화 완료: {} / 타입: {} / 강도: {} / 자산: {}", botId, botType, intensity, bot.getAsset());
		}
	}

	private void initBotHaveStocks() {
		botCache.getAll().forEach((botId, bot) -> {
			List<BotHaveStock> stocks = getOrCreateBotHaveStocks(bot, botId);
			botHaveStockCache.register(botId, stocks);
			log.info("봇 HaveStock 초기화 완료: {} / 종목 수: {}", botId, stocks.size());
		});
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
			hs.setAveragePrice(stockCache.get(stockCode).getYesterdayClosePrice());
			return hs;
		}).toList();

		return botHaveStockRepository.saveAll(stocks);
	}

	private void initBotCache() {
		stockCache.getCache().forEach((stockCode, stock) -> {
			StockRealTimeSnapshot copiedStock = stock.botCacheCopy();
			botStockCache.put(stockCode, copiedStock);
			marketStateHolder.updateMarketState(stockCode);
		});
		log.info("BotStockCache 초기화 완료: {}", stockCache.getCache().size());
	}
}