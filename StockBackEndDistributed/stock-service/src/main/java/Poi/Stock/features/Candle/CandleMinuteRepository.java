package Poi.Stock.features.Candle;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CandleMinuteRepository extends JpaRepository<CandleMinute, Long> {
	List<CandleMinute> findByStockCodeAndTimeAfter(String stockCode, LocalDateTime time);

	@Query("SELECT DISTINCT c.stockCode FROM CandleMinute c")
	List<String> findDistinctStockCodes();

	List<CandleMinute> findByStockCodeAndTimeBetweenOrderByTimeAsc(String stockCode, LocalDateTime startOfDay,
			LocalDateTime endOfDay);
}