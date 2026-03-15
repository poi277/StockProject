package Poi.Stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.WatchList.WatchList;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {

	void deleteByStockUserIdAndStockCode(String userId, String stockCode);

	List<WatchList> findByStockUserId(String userId);

	boolean existsByStockUserIdAndStockCode(String userId, String stockCode);

}
