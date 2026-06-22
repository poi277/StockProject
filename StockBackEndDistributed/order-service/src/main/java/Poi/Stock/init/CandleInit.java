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
	private final CandleRestoreService candleRestoreService;
	int MAX_CACHE_SIZE = 100;

	@PostConstruct
	public void init() {
		assignedCodeHolder.getAssignedCodes().forEach(stockCode -> {

			LocalDateTime now = LocalDateTime.now();
			LocalDate today = LocalDate.now();

			candleRestoreService.restoreMinuteCandle(stockCode, now);
			candleRestoreService.restoreDayCandle(stockCode, now);


			List<CandleMinute> rawMinutes = candleMinuteRepository.findByStockCodeOrderByTimeDesc(stockCode,
					PageRequest.of(0, MAX_CACHE_SIZE));

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

			// 시간봉 로컬 캐시 웜업
			List<CandleHour> hour = candleHourRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					now.minusHours(48), now);
			List<Candle> genericHours = new ArrayList<>(hour);
			candleCacheService.putCandles(CandleType.HOUR, stockCode, genericHours);

			// 일봉 로컬 캐시 웜업
			List<CandleDay> day = candleDayRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
					today.minusDays(60), today);
			List<Candle> genericDays = new ArrayList<>(day);
			candleCacheService.putCandles(CandleType.DAY, stockCode, genericDays);

			log.info("[{}] 모든 차트 로컬 캐시 레이어 초기화 완료 (시간봉: {}개 / 일봉: {}개)", stockCode, hour.size(), day.size());
		});
	}
}