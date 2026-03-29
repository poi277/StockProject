package Poi.Stock.features.Stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Poi.Stock.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockScheduler {

    private final StockRepository stockRepository;
    private final StockCache stockCache;

    // 서버 시작 시 최신 주식 데이터 캐시 로드
    @PostConstruct
    public void init() {
		List<Stock> latestStocks = stockRepository.findLatestStocks();
        latestStocks.forEach(stock -> stockCache.put(stock.getStockCode(), stock));
        log.info("주식 캐시 로드 완료: {}개 종목", latestStocks.size());
    }

    // 10분마다 DB 저장
    @Scheduled(fixedRate = 600000)
    public void saveToDatabase() {
        LocalDate today = LocalDate.now();
        List<Stock> stocksToSave = new ArrayList<>();
        for (Stock cachedStock : stockCache.values()) {
            Stock newRecord = new Stock();
            newRecord.setStockCode(cachedStock.getStockCode());
            newRecord.setDate(today);
            newRecord.setStockName(cachedStock.getStockName());
			newRecord.setOpenPrice(cachedStock.getOpenPrice()); // getHighPrice → getOpenPrice
			newRecord.setHighPrice(cachedStock.getHighPrice()); // 누락된 highPrice 추가
            newRecord.setLowPrice(cachedStock.getLowPrice());
            newRecord.setClosePrice(cachedStock.getClosePrice());
			newRecord.setTotalvolume(cachedStock.getTotalvolume());
            newRecord.setValue(cachedStock.getValue());
            newRecord.setChangeAmount(cachedStock.getChangeAmount());
            newRecord.setChangeRate(cachedStock.getChangeRate());
            stocksToSave.add(newRecord);
        }
        stockRepository.saveAll(stocksToSave);
        log.info("DB 저장 완료: {}건", stocksToSave.size());
    }
}
