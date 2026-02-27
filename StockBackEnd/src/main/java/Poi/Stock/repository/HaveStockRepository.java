package Poi.Stock.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
public interface HaveStockRepository extends JpaRepository<HaveStock, Long> {

	// 특정 사용자의 특정 종목 보유 정보 조회
	Optional<HaveStock> findByStockUserAndStockCode(StockUser stockUser, String stockCode);

	// 특정 사용자의 모든 보유 주식 조회
	List<HaveStock> findByStockUser(StockUser stockUser);

	// 특정 종목을 보유한 모든 사용자 조회
	List<HaveStock> findByStockCode(String stockCode);

	@Query("SELECT h FROM HaveStock h WHERE h.stockUser.id IN :userIds AND h.stockCode = :stockCode")
	List<HaveStock> findByUserIdsAndStockCode(@Param("userIds") Collection<String> userIds,
			@Param("stockCode") String stockCode);
}

