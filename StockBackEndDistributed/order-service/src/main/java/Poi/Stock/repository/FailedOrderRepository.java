package Poi.Stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.FailedOrder.FailedOrder;

public interface FailedOrderRepository extends JpaRepository<FailedOrder, Long> {
}