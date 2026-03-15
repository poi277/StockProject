package Poi.Stock.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockDailyPriceId;

public interface StockRepository extends JpaRepository<Stock, StockDailyPriceId> {

	// 특정 종목의 최신 데이터
	Optional<Stock> findFirstByStockCodeOrderByDateDesc(String stockCode);

	// 특정 종목의 특정 날짜 데이터
	Optional<Stock> findByStockCodeAndDate(String stockCode, LocalDate date);

	// 특정 종목의 기간별 데이터
	List<Stock> findByStockCodeAndDateBetweenOrderByDateDesc(String stockCode, LocalDate startDate, LocalDate endDate);

	// 특정 날짜의 모든 종목
	List<Stock> findByDate(LocalDate date);

	// 모든 종목의 최신 데이터 조회
	@Query("SELECT s FROM Stock s WHERE s.date = "
			+ "(SELECT MAX(s2.date) FROM Stock s2 WHERE s2.stockCode = s.stockCode)")
	List<Stock> findLatestStocks();
}