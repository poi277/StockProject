package Poi.Stock.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Poi.Stock.features.Candle.CandleMinute;

public interface CandleMinuteRepository extends JpaRepository<CandleMinute, Long> {

	List<CandleMinute> findByStockCodeAndTimeAfterOrderByTimeAsc(String stockCode, LocalDateTime from);

	List<CandleMinute> findByStockCodeAndTimeBetweenOrderByTimeAsc(String stockCode, LocalDateTime start,
			LocalDateTime end);

	@Query("SELECT DISTINCT c.stockCode FROM CandleMinute c")
	List<String> findDistinctStockCodes();
}