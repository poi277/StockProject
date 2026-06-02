package Poi.Stock.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Candle.CandleSchedulerService;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleHourRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.util.AssignedCodeHolder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component("candleInit")
@DependsOn("orderInit")
@RequiredArgsConstructor
public class CandleInit {

	private final CandleCacheService candleCacheService;
    private final CandleMinuteRepository candleMinuteRepository;
    private final CandleHourRepository candleHourRepository;
    private final CandleDayRepository candleDayRepository;
	private final CandleSchedulerService candleSchedulerService;
	private final AssignedCodeHolder assignedCodeHolder;


	@PostConstruct
	public void init() {

		assignedCodeHolder.getAssignedCodes().forEach(stockCode -> {

			LocalDateTime now = LocalDateTime.now();
			LocalDate today = LocalDate.now();

			List<CandleMinute> rawMinutes = candleMinuteRepository
					.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, now.minusMinutes(100), now);

			List<CandleMinute> oneMin = rawMinutes.stream().filter(c -> !c.getTime().isBefore(now.minusMinutes(20)))
					.collect(Collectors.toList());

			candleCacheService.putOneMinCandles(stockCode, oneMin);

			List<CandleMinute> fiveMin = candleSchedulerService.convertToMinute(rawMinutes, 5);

			candleCacheService.putFiveMinCandles(stockCode, fiveMin);

			List<CandleHour> hour = candleHourRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					now.minusHours(20), now);

			candleCacheService.putHourCandles(stockCode, hour);

			List<CandleDay> day = candleDayRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
					today.minusDays(20), today);

			candleCacheService.putDayCandles(stockCode, day);

			log.info("캔들 캐시 초기화 완료: {} / 1분봉: {}개 / 5분봉: {}개 / 시간봉: {}개 / 일봉: {}개", stockCode, oneMin.size(),
					fiveMin.size(), hour.size(), day.size());
		});
	}
}