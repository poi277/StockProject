package Poi.Stock.features.WatchList;

import java.util.List;

import org.springframework.stereotype.Service;

import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.repository.WatchListRepository;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final StockUserRepository stockUserRepository;

	public void addWatch(String userId, String stockCode) {
        StockUser user = stockUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));
        WatchList watchList = new WatchList();
        watchList.setStockUser(user);
        watchList.setStockCode(stockCode);
        watchListRepository.save(watchList);
    }

	public void removeWatch(String userId, String stockCode) {
        watchListRepository.deleteByStockUserIdAndStockCode(userId, stockCode);
    }

	public List<WatchList> getWatchList(String userId) {
		return watchListRepository.findByStockUserId(userId);
    }
}