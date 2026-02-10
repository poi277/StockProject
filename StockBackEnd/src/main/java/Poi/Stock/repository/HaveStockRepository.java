package Poi.Stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;

public interface HaveStockRepository extends JpaRepository<HaveStock, Long> {
	Optional<HaveStock> findByStockUserAndStock(StockUser stockUser, Stock stock);
	List<HaveStock> findByUser(StockUser user);
}

