package Poi.Stock.features.Candle.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Poi.Stock.features.Candle.Entity.CandleWeek;

@Repository
public interface CandleWeekRepository extends JpaRepository<CandleWeek, Long> {

	// 특정 종목의 특정 기간 동안의 주봉을 날짜순(오름차순)으로 조회 (차트 그리기용)
	List<CandleWeek> findByStockCodeAndDateBetweenOrderByDateAsc(String stockCode, LocalDate startDate,
			LocalDate endDate);

	// 특정 종목의 특정 날짜 주봉 단건 조회
	Optional<CandleWeek> findByStockCodeAndDate(String stockCode, LocalDate date);

	// 전체 주봉 데이터에서 존재하는 종목 코드 목록 추출
	@Query("SELECT DISTINCT c.stockCode FROM CandleWeek c")
	List<String> findDistinctStockCodes();


	// 가장 최근 주봉 단건 조회
	Optional<CandleWeek> findTopByStockCodeOrderByDateDesc(String stockCode);

	// 🎯 [수정 완료] 기존 OrderByTimeDesc를 Date 규격으로 안전하게 변경 (페이징용)
	List<CandleWeek> findByStockCodeOrderByDateDesc(String stockCode, Pageable pageable);

	// 🎯 [정상 반영] loadTop100FromDb 메서드와 완벽히 호환되는 Top 100 조회 메서드
	List<CandleWeek> findTop100ByStockCodeOrderByDateDesc(String stockCode);
}