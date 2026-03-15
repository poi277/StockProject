package Poi.Stock.features.Stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.getAssetDTO;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.UserAssetService;
import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;
	private final StockCache stockCache;
	private final UserAssetService userAssetService; // ← StockUserRepository, HaveStockRepository 대체

	public List<Stock> getAllStocks() {
		return new ArrayList<>(stockCache.values());
	}

	public List<HaveStock> getMyStocks(String userId) {
		return userAssetService.getMyStocks(userId);
	}

	public Stock getStock(String stockCode) {
		Stock stock = stockCache.get(stockCode);
		if (stock != null)
			return stock;
		stock = stockRepository.findFirstByStockCodeOrderByDateDesc(stockCode)
				.orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다: " + stockCode));
		stockCache.put(stockCode, stock);
		return stock;
	}

	public Stock getStockByDate(String stockCode, LocalDate date) {
		return stockRepository.findByStockCodeAndDate(stockCode, date)
				.orElseThrow(() -> new RuntimeException("해당 날짜의 데이터가 없습니다"));
	}

	public List<Stock> getStockHistory(String stockCode, LocalDate startDate, LocalDate endDate) {
		return stockRepository.findByStockCodeAndDateBetweenOrderByDateDesc(stockCode, startDate, endDate);
	}

	public getAssetDTO getMyAsset() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userId = authentication.getName();
		return userAssetService.getMyAsset(userId);
	}

	public void updateCurrentPrice(String stockCode, int lastFillPrice) {
		Stock stock = stockCache.get(stockCode);
		if (stock == null || stock.getClosePrice() == lastFillPrice)
			return;
		stock.setClosePrice(lastFillPrice);
		stock.setHighPrice(Math.max(stock.getHighPrice(), lastFillPrice));
		stock.setLowPrice(Math.min(stock.getLowPrice(), lastFillPrice));
		int changeAmount = lastFillPrice - stock.getOpenPrice();
		stock.setChangeAmount(changeAmount);
		stock.setChangeRate((double) changeAmount / stock.getOpenPrice() * 100);
		stockCache.put(stockCode, stock);
	}
}
