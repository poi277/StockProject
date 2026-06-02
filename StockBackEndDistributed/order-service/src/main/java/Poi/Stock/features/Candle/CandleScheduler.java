package Poi.Stock.features.Candle;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.util.AssignedCodeHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor

public class CandleScheduler {

	private final CandleSchedulerService candleSaveService;
	private final AssignedCodeHolder assignedCodeHolder;

	@Scheduled(cron = "0 * * * * *")
	public void candleScheduler() {

		List<String> assignedCodes = assignedCodeHolder.getAssignedCodes();

		List<CandleMinute> savedCandles = candleSaveService.save1MinCandle(assignedCodes);

		candleSaveService.updateMinuteCaches(savedCandles);

		LocalDateTime now = LocalDateTime.now();

		// 매시간 정각
		if (now.getMinute() == 0) {
			candleSaveService.saveHourlyCandles(assignedCodes);
		}

		// 자정
		if (now.getHour() == 0 && now.getMinute() == 0) {
			candleSaveService.saveDailyCandles(assignedCodes);
		}
	}
}