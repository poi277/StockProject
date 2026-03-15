package Poi.Stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.CompletedOrder.CompletedOrder;

public interface CompletedOrderRepository extends JpaRepository<CompletedOrder, Long> {

}
