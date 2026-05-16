package Poi.Stock.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Poi.Stock.features.Stock.CandleMinute;

public interface CandleMinuteRepository extends JpaRepository<CandleMinute, Long> {
	List<CandleMinute> findByStockCodeAndTimeAfter(String stockCode, LocalDateTime time);

	@Query("SELECT DISTINCT c.stockCode FROM CandleMinute c")
	List<String> findDistinctStockCodes();
}