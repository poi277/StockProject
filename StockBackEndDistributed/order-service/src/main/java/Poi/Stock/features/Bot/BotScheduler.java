package Poi.Stock.features.Bot;

import java.util.List;

import org.springframework.stereotype.Component;

import Poi.Stock.util.EnumUtil.BotType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotScheduler {

	private final MarketStateHolder marketStateHolder;
	private final BotCache botCache;

	// 🎯 1. 개인 봇 매매 주기: 1초마다 실행 (fixedDelay = 1000)
	// @Scheduled(fixedDelay = 1000)
	public void runIndividualBots() {
		List<AbstractBot> runningBots = botCache.getRunningBotsCache();

		runningBots.stream().filter(bot -> bot.getBotType() == BotType.INDIVIDUAL).forEach(bot -> {
			try {
				bot.placeOrders();
			} catch (Exception e) {
				log.error("개인 봇 실행 오류 - 계정: {}, 에러: {}", bot.getBotId(), e.getMessage());
			}
		});
	}

	// 2. 외국인 봇 매매 주기: 2초마다 실행 (fixedDelay = 2000)
	// @Scheduled(fixedDelay = 2000)
	public void runForeignBots() {
		List<AbstractBot> runningBots = botCache.getRunningBotsCache();

		runningBots.stream().filter(bot -> bot.getBotType() == BotType.FOREIGN).forEach(bot -> {
			try {
				bot.placeOrders();
			} catch (Exception e) {
				log.error("외국인 봇 실행 오류 - 계정: {}, 에러: {}", bot.getBotId(), e.getMessage());
			}
		});
	}

	// 3. 기관 봇 매매 주기: 10초마다 실행 (fixedDelay = 10000)
	// @Scheduled(fixedDelay = 10000)
	public void runInstitutionBots() {
		List<AbstractBot> runningBots = botCache.getRunningBotsCache();

		runningBots.stream().filter(bot -> bot.getBotType() == BotType.INSTITUTION).forEach(bot -> {
			try {
				bot.placeOrders();
			} catch (Exception e) {
				log.error("기관 봇 실행 오류 - 계정: {}, 에러: {}", bot.getBotId(), e.getMessage());
			}
		});
	}

	// 🎯 4. 시장 상태 업데이트 주기: 60초마다 실행
	// @Scheduled(fixedDelay = 30000)
	public void updateMarketState() {
		try {
			marketStateHolder.updateAllStocks();
			// marketStateHolder.updateRadomStocksState();
		} catch (Exception e) {
			log.error("시장 상태 업데이트 오류: {}", e.getMessage());
		}
	}
}