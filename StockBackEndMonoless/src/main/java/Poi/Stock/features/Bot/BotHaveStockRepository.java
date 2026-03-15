package Poi.Stock.features.Bot;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BotHaveStockRepository extends JpaRepository<BotHaveStock, Long> {

	List<BotHaveStock> findByBot_BotId(String botId);

	Optional<BotHaveStock> findByBot_BotIdAndStockCode(String botId, String stockCode);
}