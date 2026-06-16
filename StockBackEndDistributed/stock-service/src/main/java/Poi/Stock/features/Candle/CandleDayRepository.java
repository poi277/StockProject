package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CandleDayRepository extends JpaRepository<CandleDay, Long> {

	// 특정 종목의 특정 기간 동안의 일봉을 날짜순(오름차순)으로 조회 (차트 그리기용)
	List<CandleDay> findByStockCodeAndDateBetweenOrderByDateAsc(String stockCode, LocalDate startDate,
			LocalDate endDate);

	// 특정 종목의 특정 날짜 일봉 단건 조회 (전일 종가 등을 확인할 때 사용)
	Optional<CandleDay> findByStockCodeAndDate(String stockCode, LocalDate date);

	// 전체 일봉 데이터에서 존재하는 종목 코드 목록 추출
	@Query("SELECT DISTINCT c.stockCode FROM CandleDay c")
	List<String> findDistinctStockCodes();

	Optional<CandleDay> findTopByStockCodeOrderByDateDesc(String stockCode);
}