package Poi.Stock.features.User;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.getAssetDTO;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAssetService {

	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;

	public void validateOrder(String userId, tradeType type, String stockCode, int price, int quantity) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		if (type == tradeType.BUY) {
			int totalCost = price * quantity;
			if (user.getAsset() < totalCost)
				throw new RuntimeException(String.format("자산이 부족합니다. 필요: %d원, 보유: %d원", totalCost, user.getAsset()));
		}
		if (type == tradeType.SELL) {
			HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, stockCode)
					.orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
			if (haveStock.getQuantity() < quantity)
				throw new RuntimeException(
						String.format("보유 수량이 부족합니다. 보유: %d주, 매도 요청: %d주", haveStock.getQuantity(), quantity));
		}
	}

	@Transactional
	public void refundAsset(String userId, int refundAmount) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		user.setAsset(user.getAsset() + refundAmount);
		stockUserRepository.save(user);
		log.info("자산 환불: userId={}, amount={}", userId, refundAmount);
	}

	public List<HaveStock> getMyStocks(String userId) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		return haveStockRepository.findByStockUser(user);
	}

	public getAssetDTO getMyAsset(String userId) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		List<getAssetDTO.HoldingDTO> holdingDTOs = user.getHoldings().stream()
				.map(h -> new getAssetDTO.HoldingDTO(h.getStockCode(), h.getQuantity(), h.getAveragePrice()))
				.collect(Collectors.toList());
		return new getAssetDTO(user.getAsset(), holdingDTOs);
	}
}