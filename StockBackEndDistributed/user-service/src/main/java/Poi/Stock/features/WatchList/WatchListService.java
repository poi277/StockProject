package Poi.Stock.features.WatchList;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.repository.WatchListRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final StockUserRepository stockUserRepository;

    // StockService 제거 - getWatchListWithPrice는 stockCode 목록만 반환
    // stock-service 분리 후 프론트에서 stockCode로 직접 조회하거나
    // stock-service Feign Client로 교체 예정

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

    // stockCode 목록 반환 (stock-service 분리 전 임시)
    public List<String> getWatchListStockCodes(String userId) {
        return watchListRepository.findByStockUserId(userId)
                .stream()
                .map(WatchList::getStockCode)
                .collect(Collectors.toList());
    }
}
