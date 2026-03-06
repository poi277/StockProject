package Poi.Stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.TreadeHistory.TradeHistory;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

}
