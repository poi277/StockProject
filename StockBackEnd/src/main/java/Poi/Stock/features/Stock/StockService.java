package Poi.Stock.features.Stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.getAssetDTO;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.features.Websocket.StockCache;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.repository.StockUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;
	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;
	private final StockCache stockCache;

	// 전체 주식 조회
	public List<Stock> getAllStocks() {
		return new ArrayList<>(stockCache.values());
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
