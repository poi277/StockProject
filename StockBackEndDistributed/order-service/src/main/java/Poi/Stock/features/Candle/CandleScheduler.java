package Poi.Stock.features.Candle;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CandleScheduler {

	private final CandleSchedulerService candleSaveService;

	@Scheduled(fixedRate = 60000)
	public void save1MinCandle() {
		candleSaveService.save1MinCandle();
	}
}