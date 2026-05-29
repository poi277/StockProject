package Poi.Stock.features.Candle.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Poi.Stock.features.Candle.Entity.CandleHour;

@Repository
public interface CandleHourRepository extends JpaRepository<CandleHour, Long> {

	// 특정 종목의 특정 기간 동안의 60분봉을 시간순(오름차순)으로 조회
	List<CandleHour> findByStockCodeAndTimeBetweenOrderByTimeAsc(String stockCode, LocalDateTime startTime,
			LocalDateTime endTime);

	// 스케줄러 집계 시 필요한 유효한 종목 코드 목록 추출
	@Query("SELECT DISTINCT c.stockCode FROM CandleHour c")
	List<String> findDistinctStockCodes();
}