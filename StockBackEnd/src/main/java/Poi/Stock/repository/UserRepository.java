package Poi.Stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.User.StockUser;

public interface UserRepository extends JpaRepository<StockUser, String> {
}

