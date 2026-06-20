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
	int MAX_CACHE_SIZE = 100;

	@PostConstruct
	public void init() {
		assignedCodeHolder.getAssignedCodes().forEach(stockCode -> {
			LocalDateTime now = LocalDateTime.now();
			LocalDate today = LocalDate.now();

			List<CandleMinute> rawMinutes = candleMinuteRepository.findByStockCodeOrderByTimeDesc(stockCode,
					PageRequest.of(0, MAX_CACHE_SIZE));

			// 컬렉션 순서 뒤집기 (불변 리스트일 가능성을 대비해 수정 가능한 리스트인지 확인 필요)
			List<CandleMinute> mutableRawMinutes = new ArrayList<>(rawMinutes);
			Collections.reverse(mutableRawMinutes);

			if (mutableRawMinutes.isEmpty()) {
				log.warn("[{}] 초기화할 1분봉 데이터가 없습니다.", stockCode);
				return;
			}

			// 1. 분봉 계열 캐시 웜업
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

				log.info("캔들 캐시 웜업 완료 -> 종목: {} | 타입: {} | 적재 개수: {}", stockCode, type.name(), processedMinutes.size());
			}
			List<CandleHour> hour = candleHourRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					now.minusHours(48), now);
			List<Candle> genericHours = new ArrayList<>(hour);
			candleCacheService.putCandles(CandleType.HOUR, stockCode, genericHours);

			// 3. 일봉 캐시 웜업
			List<CandleDay> day = candleDayRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode,
					today.minusDays(60), today);
			List<Candle> genericDays = new ArrayList<>(day);
			candleCacheService.putCandles(CandleType.DAY, stockCode, genericDays);

			log.info("[{}] 모든 차트 캐시 초기화 완료 (시간봉: {}개 / 일봉: {}개)", stockCode, hour.size(), day.size());
		});
	}
}