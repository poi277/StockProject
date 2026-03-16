package Poi.Stock.features.WatchList;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.repository.WatchListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final StockUserRepository stockUserRepository;
	private final RestTemplate restTemplate;
	@Value("${stock.service.url}")
	private String stockServiceUrl;

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

	public List<Object> getWatchListWithStockInfo(String userId) {
		List<String> stockCodes = watchListRepository.findByStockUserId(userId).stream().map(WatchList::getStockCode)
				.collect(Collectors.toList());
		;
		return stockCodes.stream().map(stockCode -> {
			try {
				ResponseEntity<Object> response = restTemplate
						.getForEntity(stockServiceUrl + "/stock/watch/" + stockCode,
						Object.class);
				return response.getBody();
			} catch (Exception e) {
				log.error("stock-service 조회 실패: {} / {}", stockCode, e.getMessage());
				return java.util.Map.of("stockCode", stockCode);
			}
		}).collect(Collectors.toList());
	}
}
