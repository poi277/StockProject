package Poi.Stock.features.Stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.getAssetDTO;
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

	// 웹소켓을 위해 메모리에 주식 정보 저장 (종목코드별 최신 데이터)
	private Map<String, Stock> stockCache = new ConcurrentHashMap<>();

	// 서버 시작시 DB에서 각 종목의 최신 데이터만 로드
	@PostConstruct
	public void init() {
		// 모든 종목의 최신 데이터 조회
		List<Stock> latestStocks = stockRepository.findLatestStocks();

		// 캐시에 저장
		latestStocks.forEach(stock -> stockCache.put(stock.getStockCode(), stock));

		log.info("주식 {} 개 로드 완료", latestStocks.size());
	}

	// 10분마다 DB에 저장 (새로운 날짜 레코드로 저장)
	@Scheduled(fixedRate = 600000)
	public void saveToDatabase() {
		LocalDate today = LocalDate.now();

		// 각 종목의 현재 상태를 오늘 날짜로 저장
		List<Stock> stocksToSave = new ArrayList<>();

		for (Stock cachedStock : stockCache.values()) {
			Stock newRecord = new Stock();
			newRecord.setStockCode(cachedStock.getStockCode());
			newRecord.setDate(today);
			newRecord.setStockName(cachedStock.getStockName());
			newRecord.setOpenPrice(cachedStock.getOpenPrice());
			newRecord.setHighPrice(cachedStock.getHighPrice());
			newRecord.setLowPrice(cachedStock.getLowPrice());
			newRecord.setClosePrice(cachedStock.getClosePrice());
			newRecord.setVolume(cachedStock.getVolume());
			newRecord.setValue(cachedStock.getValue());
			newRecord.setChangeAmount(cachedStock.getChangeAmount());
			newRecord.setChangeRate(cachedStock.getChangeRate());

			stocksToSave.add(newRecord);
		}

		stockRepository.saveAll(stocksToSave);
		log.info("DB 저장 완료 - {} 건", stocksToSave.size());
	}

	// 가격 업데이트 및 WebSocket 전송
	private void updateStockPrice(Stock stock, int priceChange) {
		// 기존 가격 저장
		int oldPrice = stock.getClosePrice();
		int newPrice = oldPrice + priceChange;
		newPrice = Math.max(100, newPrice); // 최소 100원

		// 고가/저가 업데이트
		if (newPrice > stock.getHighPrice()) {
			stock.setHighPrice(newPrice);
		}
		if (newPrice < stock.getLowPrice()) {
			stock.setLowPrice(newPrice);
		}

		// 주식 정보 업데이트
		stock.setClosePrice(newPrice);
		stock.setChangeAmount(newPrice - stock.getOpenPrice());
		stock.setChangeRate((double) (newPrice - stock.getOpenPrice()) / stock.getOpenPrice() * 100);
		stock.setVolume(stock.getVolume() + Math.abs(priceChange / 100)); // 거래량 증가
		stock.setValue((long) stock.getClosePrice() * stock.getVolume()); // 거래대금 업데이트

		// 캐시 업데이트
		stockCache.put(stock.getStockCode(), stock);

		// WebSocket으로 실시간 전송
		messagingTemplate.convertAndSend("/topic/stock/" + stock.getStockCode(), stock);
	}

	// 전체 주식 조회
	public List<Stock> getAllStocks() {
		return new ArrayList<>(stockCache.values());
	}

	@Transactional
	public void buyStock(String userId, String stockCode, int quantity) {
		// 1. 사용자 조회
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		// 2. 주식 조회 - 캐시에서 최신 가격 가져오기
		Stock stock = stockCache.get(stockCode);
		if (stock == null) {
			throw new RuntimeException("주식을 찾을 수 없습니다");
		}

		// 3. 자산 확인 (살 수 있는지)
		int totalCost = stock.getClosePrice() * quantity;
		if (user.getAsset() < totalCost) {
			throw new RuntimeException("자산이 부족합니다. " + "필요: " + totalCost + "원, " + "보유: " + user.getAsset() + "원");
		}

		// 4. 자산 차감
		user.setAsset(user.getAsset() - totalCost);
		stockUserRepository.save(user);

		// 5. 기존 보유 주식 확인
		HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, stockCode).orElse(null);

		if (haveStock == null) {
			// 새로 매수
			haveStock = new HaveStock();
			haveStock.setStockUser(user);
			haveStock.setStockCode(stockCode);
			haveStock.setQuantity(quantity);
			haveStock.setAveragePrice(stock.getClosePrice());
		} else {
			// 추가 매수 (평균 단가 계산)
			int totalQuantity = haveStock.getQuantity() + quantity;
			int totalPrice = (haveStock.getAveragePrice() * haveStock.getQuantity())
					+ (stock.getClosePrice() * quantity);

			haveStock.setQuantity(totalQuantity);
			haveStock.setAveragePrice(totalPrice / totalQuantity);
		}

		haveStockRepository.save(haveStock);

		// 6. 매수 후 가격 상승 (1주당 100원)
		updateStockPrice(stock, quantity * 100);

		log.info("매수 완료 - 사용자: {}, 종목: {}, 수량: {}주, 금액: {}원, 잔여자산: {}원", userId, stockCode, quantity, totalCost,
				user.getAsset());
	}

	@Transactional
	public void sellStock(String userId, String stockCode, int quantity) {
		// 1. 사용자 조회
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		// 2. 캐시에서 주식 정보 가져오기
		Stock stock = stockCache.get(stockCode);
		if (stock == null) {
			throw new RuntimeException("주식을 찾을 수 없습니다");
		}

		// 3. 보유 주식 확인
		HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, stockCode)
				.orElseThrow(() -> new RuntimeException("보유 주식이 없습니다"));

		// 4. 보유 수량 확인
		if (haveStock.getQuantity() < quantity) {
			throw new RuntimeException(
					"보유 수량이 부족합니다. " + "보유: " + haveStock.getQuantity() + "주, " + "매도 요청: " + quantity + "주");
		}

		// 5. 자산 증가 (판 금액만큼)
		int totalRevenue = stock.getClosePrice() * quantity;
		user.setAsset(user.getAsset() + totalRevenue);
		stockUserRepository.save(user);

		// 6. 수량 차감
		haveStock.setQuantity(haveStock.getQuantity() - quantity);

		// 7. 모두 판 경우 삭제, 아니면 업데이트
		if (haveStock.getQuantity() == 0) {
			haveStockRepository.delete(haveStock);
		} else {
			haveStockRepository.save(haveStock);
		}

		// 8. 매도 후 가격 하락 (1주당 100원)
		updateStockPrice(stock, -quantity * 100);

		// 9. 수익 계산 로그
		int profit = (stock.getClosePrice() - haveStock.getAveragePrice()) * quantity;
		log.info("매도 완료", userId, stockCode, quantity,
				totalRevenue, profit, user.getAsset());
	}

	// 내 보유 주식 목록 조회
	public List<HaveStock> getMyStocks(String userId) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		return haveStockRepository.findByStockUser(user);
	}

	// 특정 주식 조회 (최신 데이터)
	public Stock getStock(String stockCode) {
		// 캐시에서 먼저 확인
		Stock stock = stockCache.get(stockCode);
		if (stock != null) {
			return stock;
		}
		// 캐시에 없으면 DB에서 최신 데이터 조회
		stock = stockRepository.findFirstByStockCodeOrderByDateDesc(stockCode)
				.orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다: " + stockCode));

		// 캐시에 저장
		stockCache.put(stockCode, stock);
		return stock;
	}

	// 특정 날짜의 주식 데이터 조회
	public Stock getStockByDate(String stockCode, LocalDate date) {
		return stockRepository.findByStockCodeAndDate(stockCode, date)
				.orElseThrow(() -> new RuntimeException("해당 날짜의 데이터가 없습니다"));
	}

	// 특정 종목의 기간별 데이터 조회 (차트용)
	public List<Stock> getStockHistory(String stockCode, LocalDate startDate, LocalDate endDate) {
		return stockRepository.findByStockCodeAndDateBetweenOrderByDateDesc(stockCode, startDate, endDate);
	}

	public getAssetDTO getMyAsset() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userId = authentication.getName();
		// 사용자 조회
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		// Holdings를 HoldingDTO로 변환
		List<getAssetDTO.HoldingDTO> holdingDTOs = user.getHoldings().stream()
				.map(holding -> new getAssetDTO.HoldingDTO(holding.getStockCode(), holding.getQuantity(),
						holding.getAveragePrice()))
				.collect(Collectors.toList());
		// DTO 생성 및 반환
		return new getAssetDTO(user.getAsset(), holdingDTOs);
	}
}
