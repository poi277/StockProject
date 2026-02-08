package Poi.Stock.features.Stock;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {
	private final StockRepository stockRepository;

	public List<Stock> getStockList() {
		return stockRepository.findAll();
	}

	public Optional<Stock> getStock(String stockId) {
		return stockRepository.findById(stockId);
	}
}

