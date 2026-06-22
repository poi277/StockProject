package Poi.Stock.features.Candle.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Poi.Stock.features.Candle.Entity.CandleMinute;

public interface CandleMinuteRepository extends JpaRepository<CandleMinute, Long> {

	List<CandleMinute> findByStockCodeAndTimeAfterOrderByTimeAsc(String stockCode, LocalDateTime from);

	List<CandleMinute> findByStockCodeAndTimeBetweenOrderByTimeAsc(String stockCode, LocalDateTime start,
			LocalDateTime end);

	@Query("SELECT DISTINCT c.stockCode FROM CandleMinute c")
	List<String> findDistinctStockCodes();

	List<CandleMinute> findByStockCodeAndTimeAfter(String stockCode, LocalDateTime time);

	List<CandleMinute> findByStockCodeOrderByTimeDesc(String stockCode, PageRequest of);

	List<CandleMinute> findTop100ByStockCodeOrderByTimeDesc(String stockCode);

	Optional<CandleMinute> findTopByStockCodeOrderByTimeDesc(String stockCode);

}