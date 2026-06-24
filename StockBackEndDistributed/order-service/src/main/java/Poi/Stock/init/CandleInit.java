package Poi.Stock.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import Poi.Stock.features.Candle.CandleCacheService;
import Poi.Stock.features.Candle.CandleRestoreService;
import Poi.Stock.features.Candle.CandleSchedulerService;
import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleMonth;
import Poi.Stock.features.Candle.Entity.CandleWeek;
import Poi.Stock.features.Candle.Entity.CandleYear;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleHourRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.Candle.repository.CandleMonthRepository;
import Poi.Stock.features.Candle.repository.CandleWeekRepository;
import Poi.Stock.features.Candle.repository.CandleYearRepository;
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
	private final CandleWeekRepository candleWeekRepository;
	private final CandleMonthRepository candleMonthRepository;
	private final CandleYearRepository candleYearRepository;
	private final CandleSchedulerService candleSchedulerService;
	private final AssignedCodeHolder assignedCodeHolder;
	private final CandleRestoreService candleRestoreService;
	int MAX_CACHE_SIZE = 100;
	PageRequest pageRequest = PageRequest.of(0, MAX_CACHE_SIZE);

	@PostConstruct
	public void init() {
		assignedCodeHolder.getAssignedCodes().forEach(stockCode -> {

			LocalDateTime now = LocalDateTime.now();
			LocalDate today = LocalDate.now();

			candleRestoreService.restoreMinuteCandle(stockCode, now);
			candleRestoreService.restoreDayCandle(stockCode, now);


			List<CandleMinute> rawMinutes = candleMinuteRepository.findByStockCodeOrderByTimeDesc(stockCode,
					pageRequest);

			List<CandleMinute> mutableRawMinutes = new ArrayList<>(rawMinutes);
			Collections.reverse(mutableRawMinutes);

			if (mutableRawMinutes.isEmpty()) {
				log.warn("[{}] 초기화할 1분봉 데이터가 영구히 없습니다.", stockCode);
				return;
			}

			// 분봉 계열 로컬 메모리 캐시 웜업
			for (CandleType type : CandleType.values()) {
				if (!type.isMinuteType()) {
					continue;
				}

				List<CandleMinute> processedMinutes;
				if (type == CandleType.ONE_MINUTE) {
					processedMinutes = mutableRawMinutes;
				} else {
					processedMinutes = candleSchedulerService.convertToMinute(mutableRawMinutes, type.getMinute());
				}
				List<Candle> genericMinutes = new ArrayList<>(processedMinutes);
				candleCacheService.putCandles(type, stockCode, genericMinutes);
			}

			List<CandleHour> rawHours = candleHourRepository.findByStockCodeOrderByTimeDesc(stockCode, pageRequest);
			candleCacheService.putCandles(CandleType.HOUR, stockCode, processAndConvertToGeneric(rawHours));
			// 2. 일봉 웜업
			List<CandleDay> rawDays = candleDayRepository.findByStockCodeOrderByDateDesc(stockCode, pageRequest);
			candleCacheService.putCandles(CandleType.DAY, stockCode, processAndConvertToGeneric(rawDays));
			// 3. 주봉 웜업
			List<CandleWeek> rawWeeks = candleWeekRepository.findByStockCodeOrderByDateDesc(stockCode, pageRequest);
			candleCacheService.putCandles(CandleType.WEEK, stockCode, processAndConvertToGeneric(rawWeeks));
			// 4. 월봉 웜업
			List<CandleMonth> rawMonths = candleMonthRepository.findByStockCodeOrderByDateDesc(stockCode, pageRequest);
			candleCacheService.putCandles(CandleType.MONTH, stockCode, processAndConvertToGeneric(rawMonths));
			// 5. 년봉 웜업
			List<CandleYear> rawYears = candleYearRepository.findByStockCodeOrderByDateDesc(stockCode, pageRequest);
			candleCacheService.putCandles(CandleType.YEAR, stockCode, processAndConvertToGeneric(rawYears));

			log.info("[{}] 모든 차트 로컬 캐시 레이어 초기화 완료 (시간봉: {}개 / 일봉: {}개 / 주봉: {}개 / 월봉: {}개 / 년봉: {}개)", stockCode,
					rawHours.size(), rawDays.size(), rawWeeks.size(), rawMonths.size(), rawYears.size());
		});
	}

	private <T extends Candle> List<Candle> processAndConvertToGeneric(List<T> rawCandles) {
		// 굳이 복잡하게 복사본을 만들어서 .sort()를 돌리는 것보다
		// 가변 리스트(ArrayList)로 감싼 뒤 한 방에 뒤집는(Reverse) 게 성능과 가독성 모두 최선입니다.
		List<T> mutableList = new ArrayList<>(rawCandles);
		Collections.reverse(mutableList); // Desc -> Asc 변환

		return new ArrayList<>(mutableList); // List<T>를 List<Candle> 타입으로 안전하게 캐스팅하여 반환
	}
}