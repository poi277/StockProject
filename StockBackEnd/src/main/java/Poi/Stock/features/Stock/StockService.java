package Poi.Stock.features.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.repository.StockUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;
	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;
	private final SimpMessagingTemplate messagingTemplate;

	// 웹소켓을 위해 메모리에 주식 정보 저장
	private Map<String, Stock> stockCache = new ConcurrentHashMap<>();

	// 서버 시작시 DB에서 한번만 로드
	@PostConstruct
	public void init() {
		List<Stock> stocks = stockRepository.findAll();
		stocks.forEach(stock -> stockCache.put(stock.getId(), stock));
		log.info("주식 {} 개 로드 완료", stocks.size());
	}

	// 10분마다 DB에 저장
	@Scheduled(fixedRate = 600000)
	public void saveToDatabase() {
		stockRepository.saveAll(stockCache.values());
		log.info("DB 저장 완료");
	}

	// 가격 업데이트 및 WebSocket 전송
	private void updateStockPrice(Stock stock, int priceChange) {
		// 기존 가격 저장
		int oldPrice = stock.getPrice();
		int newPrice = oldPrice + priceChange;
		newPrice = Math.max(100, newPrice); // 최소 100원

		// 주식 정보 업데이트
		stock.setPrice(newPrice);
		stock.setChangeAmount(newPrice - oldPrice);
		stock.setChangeRate((double) (newPrice - oldPrice) / oldPrice * 100);
		stock.setVolume(stock.getVolume() + Math.abs(priceChange / 100)); // 거래량 증가
		// WebSocket으로 실시간 전송
		messagingTemplate.convertAndSend("/topic/stock/" + stock.getId(), stock);
	}

	// 전체 주식 조회
	public List<Stock> getAllStocks() {
		return new ArrayList<>(stockCache.values());
	}

	@Transactional
	public void buyStock(String userId, String stockId, int quantity) {
		// 1. 사용자 조회
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		// 2. 주식 조회 - 캐시에서 가져오기
		Stock stock = stockCache.get(stockId);
		if (stock == null) {
			throw new RuntimeException("주식을 찾을 수 없습니다");
		}

		// 3. 기존 보유 주식 확인
		HaveStock haveStock = haveStockRepository.findByStockUserAndStock(user, stock).orElse(null);

		if (haveStock == null) {
			// 새로 매수
			haveStock = new HaveStock();
			haveStock.setStockUser(user);
			haveStock.setStock(stock);
			haveStock.setQuantity(quantity);
			haveStock.setAveragePrice(stock.getPrice());
		} else {
			// 추가 매수 (평균 단가 계산)
			int totalQuantity = haveStock.getQuantity() + quantity;
			int totalPrice = (haveStock.getAveragePrice() * haveStock.getQuantity()) + (stock.getPrice() * quantity);

			haveStock.setQuantity(totalQuantity);
			haveStock.setAveragePrice(totalPrice / totalQuantity);
		}

		haveStockRepository.save(haveStock);

		// 4. 매수 후 가격 상승 (1주당 100원)
		updateStockPrice(stock, quantity * 100);
	}

	@Transactional
	public void sellStock(String userId, String stockId, int quantity) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		// 캐시에서 가져오기
		Stock stock = stockCache.get(stockId);
		if (stock == null) {
			throw new RuntimeException("주식을 찾을 수 없습니다");
		}

		HaveStock haveStock = haveStockRepository.findByStockUserAndStock(user, stock)
				.orElseThrow(() -> new RuntimeException("보유 주식이 없습니다"));

		if (haveStock.getQuantity() < quantity) {
			throw new RuntimeException("보유 수량이 부족합니다");
		}

		haveStock.setQuantity(haveStock.getQuantity() - quantity);

		// 모두 판 경우 삭제
		if (haveStock.getQuantity() == 0) {
			haveStockRepository.delete(haveStock);
		} else {
			haveStockRepository.save(haveStock);
		}

		// 5. 매도 후 가격 하락 (1주당 100원)
		updateStockPrice(stock, -quantity * 100);
	}

	// 내 보유 주식 목록 조회
	public List<HaveStock> getMyStocks(String userId) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		return haveStockRepository.findByStockUser(user);
	}
}
