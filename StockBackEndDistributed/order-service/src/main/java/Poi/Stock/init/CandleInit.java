package Poi.Stock.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCache;
import Poi.Stock.features.Candle.CandleSchedulerService;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleHourRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.Stock.StockCache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component("candleInit")
@DependsOn("stockInit")
@RequiredArgsConstructor
public class CandleInit {

    private final CandleCache candleCache;
    private final CandleMinuteRepository candleMinuteRepository;
    private final CandleHourRepository candleHourRepository;
    private final CandleDayRepository candleDayRepository;
	private final CandleSchedulerService candleSchedulerService;
    private final StockCache stockCache;

    @PostConstruct
    public void init() {
        stockCache.getCache().keySet().forEach(stockCode -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();

            // 1분봉 최근 20개
            List<CandleMinute> oneMin = candleMinuteRepository
                    .findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, now.minusMinutes(20), now);
            candleCache.putOneMin(stockCode, oneMin);

            // 5분봉 - 1분봉 100개 가져와서 5분봉 20개로 변환
            List<CandleMinute> rawFiveMin = candleMinuteRepository
                    .findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, now.minusMinutes(100), now);
			List<CandleMinute> fiveMin = candleSchedulerService.convertToFiveMin(rawFiveMin);
			candleCache.putFiveMin(stockCode, fiveMin);

            // 60분봉 최근 20개
            List<CandleHour> hour = candleHourRepository
                    .findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, now.minusHours(20), now);
            candleCache.putHour(stockCode, hour);

            // 일봉 최근 20개
            List<CandleDay> day = candleDayRepository
                    .findByStockCodeAndDateBetweenOrderByDateAsc(stockCode, today.minusDays(20), today);
            candleCache.putDay(stockCode, day);

            log.info("캔들 캐시 초기화 완료: {} / 1분봉: {}개 / 5분봉: {}개 / 시간봉: {}개 / 일봉: {}개",
					stockCode, oneMin.size(), fiveMin.size(), hour.size(), day.size());
        });
    }
}