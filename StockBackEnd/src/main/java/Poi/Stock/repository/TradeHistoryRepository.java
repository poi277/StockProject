package Poi.Stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.TradeHistory.TradeHistory;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

}
