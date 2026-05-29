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

	// 1. 기존 유지: 1분마다 Redis 스냅샷을 1분봉으로 저장
	@Scheduled(fixedRate = 60000)
	public void save1MinCandle() {
		candleSaveService.save1MinCandle();
	}

	// 2. 신규 추가: 매 시간 정각마다 실행 (60분봉 집계 및 저장)
	// 크론식: 0초 0분 매시 매일 매월 매요일
	@Scheduled(cron = "0 0 * * * *")
	public void saveHourlyCandles() {
		log.info("--- [스케줄러] 60분봉(Hour) 집계 스케줄러 시작 ---");
		candleSaveService.saveHourlyCandles();
	}

	// 3. 신규 추가: 매일 밤 23시 59분에 실행 (하루 동안의 데이터로 일봉 저장)
	// 운영하시는 시뮬레이터 장 마감 시간에 맞게 크론식을 조절하셔도 됩니다.
	@Scheduled(cron = "0 59 23 * * *")
	public void saveDailyCandles() {
		log.info("--- [스케줄러] 일봉(Day) 집계 및 마감 스케줄러 시작 ---");
		candleSaveService.saveDailyCandles();
	}
}