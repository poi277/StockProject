package Poi.Stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.Stock.Stock;

public interface StockRepository extends JpaRepository<Stock, String> {
}

