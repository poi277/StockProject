package Poi.Stock.features.Candle.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable; // PageRequest 대신 가급적 상위 인터페이스인 Pageable 권장
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Poi.Stock.features.Candle.Entity.CandleDay;
@Repository
public interface CandleDayRepository extends JpaRepository<CandleDay, Long> {

    // 특정 종목의 특정 기간 동안의 일봉을 날짜순(오름차순)으로 조회 (차트 그리기용)
    List<CandleDay> findByStockCodeAndDateBetweenOrderByDateAsc(String stockCode, LocalDate startDate, LocalDate endDate);

    // 🎯 [정상 반영] 기존의 OrderByTimeDesc 메서드들은 전부 삭제하고 아래 규격만 남겨둡니다.
    List<CandleDay> findByStockCodeOrderByDateDesc(String stockCode, Pageable pageable);

    // 🎯 [정상 반영] 앞서 loadTop100FromDb 스위치문에서 호출할 탑 100조회 메서드
    List<CandleDay> findTop100ByStockCodeOrderByDateDesc(String stockCode);


    // 전체 일봉 데이터에서 존재하는 종목 코드 목록 추출
    @Query("SELECT DISTINCT c.stockCode FROM CandleDay c")
    List<String> findDistinctStockCodes();

	Optional<CandleDay> findTopByStockCodeOrderByDateDesc(String stockCode);

	Optional<CandleDay> findByStockCodeAndDate(String stockCode, LocalDate minusDays);

    // 가장 최근 일봉 단건 조회
}