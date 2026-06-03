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
import Poi.Stock.util.EnumUtil.CandleType; // 💡 CandleType Enum 임포트 추가
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

			// DB로부터 캐시 웜업에 필요한 기반 소스 데이터(1분봉 100개) 벌크 조회
			List<CandleMinute> rawMinutes = candleMinuteRepository
					.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, now.minusMinutes(100), now);

			// 1분봉 초기 데이터 필터링 및 캐싱 적재
			List<CandleMinute> oneMin = rawMinutes.stream().filter(c -> !c.getTime().isBefore(now.minusMinutes(20)))
					.collect(Collectors.toList());

			// 구형 putOneMinCandles 대신 표준화된 단 하나의 공통 API 사용
			candleCacheService.putCandles(CandleType.ONE_MINUTE, stockCode, oneMin);

			// 5분봉 그룹핑 연산 및 캐싱 적재
			List<CandleMinute> fiveMin = candleSchedulerService.convertToMinute(rawMinutes, 5);

			// 구형 putFiveMinCandles 대신 표준화된 단 하나의 공통 API 사용
			candleCacheService.putCandles(CandleType.FIVE_MINUTE, stockCode, fiveMin);

			// 시간봉(60분봉) 소스 조회 및 캐싱 적재
			List<CandleHour> hour = candleHourRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					now.minusHours(20), now);

			// 구형 putHourCandles 대신 표준화된 단 하나의 공통 API 사용
			candleCacheService.putCandles(CandleType.HOUR, stockCode, hour);

			// 일봉 소스 조회 및 캐싱 적재
			List<CandleDay> day = candleDayRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
					today.minusDays(20), today);

			// 구형 putDayCandles 대신 표준화된 단 하나의 공통 API 사용
			candleCacheService.putCandles(CandleType.DAY, stockCode, day);

			log.info("캔들 캐시 초기화 완료: {} / 1분봉: {}개 / 5분봉: {}개 / 시간봉: {}개 / 일봉: {}개", stockCode, oneMin.size(),
					fiveMin.size(), hour.size(), day.size());
		});
	}
}