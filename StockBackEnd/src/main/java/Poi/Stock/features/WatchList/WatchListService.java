package Poi.Stock.features.WatchList;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.features.Stock.Stock;
import Poi.Stock.features.Stock.StockService;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.repository.WatchListRepository;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final StockUserRepository stockUserRepository;
	private final StockService stockService;

	public void addWatch(String userId, String stockCode) {
        StockUser user = stockUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
        WatchList watchList = new WatchList();
        watchList.setStockUser(user);
        watchList.setStockCode(stockCode);
        watchListRepository.save(watchList);
    }

	@Transactional
	public void removeWatch(String userId, String stockCode) {
        watchListRepository.deleteByStockUserIdAndStockCode(userId, stockCode);
    }

	public List<WatchList> getWatchList(String userId) {
		return watchListRepository.findByStockUserId(userId);
    }

	public boolean isWatched(String userId, String stockCode) {
		return watchListRepository.existsByStockUserIdAndStockCode(userId, stockCode);
	}

	public List<Stock> getWatchListWithPrice(String userId) {
		List<WatchList> watchList = watchListRepository.findByStockUserId(userId);

		return watchList.stream().map(watch -> stockService.getStock(watch.getStockCode()))
				.collect(Collectors.toList());
	}
}