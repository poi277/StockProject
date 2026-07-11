package Poi.Stock.features.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.getHaveStockDTO;
import Poi.Stock.features.UserWebsocket.UserWebsocketService;
import Poi.Stock.object.SettlementEvent;
import Poi.Stock.object.SettlementEvent.haveStockChange;
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
	private final UserWebsocketService userWebsocketService;

	// UserAssetService.java - validateOrder
	public void validateOrder(String userId, tradeType type, String stockCode, int price, int quantity) {
	    StockUser user = stockUserRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

	    if (type == tradeType.BUY) {
	        int totalCost = price * quantity;
	        if (user.getAvailableAsset() < totalCost)
				throw new RuntimeException(
	                String.format("자산이 부족합니다. 필요: %d원, 보유: %d원", totalCost, user.getAvailableAsset()));
	        user.setAvailableAsset(user.getAvailableAsset() - totalCost);
	        stockUserRepository.save(user);
			userWebsocketService.sendUserAsset(user);
	    }

	    if (type == tradeType.SELL) {
	        HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, stockCode)
	                .orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
	        if (haveStock.getAvailableQuantity() < quantity)
	            throw new RuntimeException(
	                String.format("보유 수량이 부족합니다. 보유: %d주, 매도 요청: %d주",
								haveStock.getAvailableQuantity(), quantity));
	        haveStock.setAvailableQuantity(haveStock.getAvailableQuantity() - quantity);
	        haveStockRepository.save(haveStock);
	    }
	}

	public void validateEditOrder(String userId, tradeType type, String stockCode, Integer newPrice,
			Integer oldPrice, Integer newQuantity, Integer RemainingQuantity) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		if (type == tradeType.BUY) {
			int oldCost = oldPrice * RemainingQuantity;
			int newCost = newPrice * newQuantity;
			int diff = newCost - oldCost;
			if (diff > 0) {
				if (user.getAvailableAsset() < diff)
					throw new RuntimeException(
							String.format("자산이 부족합니다. 추가 필요: %d원, 보유: %d원", diff, user.getAvailableAsset()));
				user.setAvailableAsset(user.getAvailableAsset() - diff); // 추가 차감
			} else {
				user.setAvailableAsset(user.getAvailableAsset() + Math.abs(diff));
			}
			stockUserRepository.save(user);
		}

		if (type == tradeType.SELL) {
			HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, stockCode)
					.orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
			int diff = newQuantity - RemainingQuantity;
			if (diff > 0) {
				if (haveStock.getAvailableQuantity() < diff)
					throw new RuntimeException(
							String.format("보유 수량이 부족합니다. 추가 필요: %d주, 가능: %d주", diff, haveStock.getAvailableQuantity()));
				haveStock.setAvailableQuantity(haveStock.getAvailableQuantity() - diff);
			} else {
				haveStock.setAvailableQuantity(haveStock.getAvailableQuantity() + Math.abs(diff));
			}
			haveStockRepository.save(haveStock);
		}
		userWebsocketService.sendUserAsset(user);
	}

	@Transactional
	public void cancelReserve(String userId, tradeType type, String stockCode, int price, int quantity) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		if (type == tradeType.BUY) {
			int refundAmount = price * quantity;
			user.setAvailableAsset(user.getAvailableAsset() + refundAmount);
			stockUserRepository.save(user);
		}

		if (type == tradeType.SELL) {
			HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, stockCode)
					.orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
			haveStock.setAvailableQuantity(haveStock.getAvailableQuantity() + quantity);
			haveStockRepository.save(haveStock);
		}
		userWebsocketService.sendUserAsset(user);
	}

	public List<HaveStock> getMyStocks(String userId) {
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
		return haveStockRepository.findByStockUser(user);
	}

	public Map<String, Object> userHaveAsset(String userId) {
		StockUser stockUser = stockUserRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("유저 없음: " + userId));

		List<getHaveStockDTO> stocks = haveStockRepository.findByStockUser(stockUser).stream().map(h -> {
			getHaveStockDTO dto = new getHaveStockDTO();
			dto.setId(h.getId());
			dto.setStockCode(h.getStockCode());
			dto.setAveragePrice((int) h.getAveragePrice());
			dto.setQuantity(h.getQuantity());
			dto.setAvailableQuantity(h.getAvailableQuantity());
			return dto;
		}).collect(Collectors.toList());

		return Map.of("haveStocks", stocks, "asset", stockUser.getAsset(), "availableAsset",
				stockUser.getAvailableAsset());
	}

	public void applySettlement(SettlementEvent event) {
		List<String> userIds = event.getStockChanges().stream().map(haveStockChange::getUserId).toList();

		Map<String, StockUser> userMap = stockUserRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(StockUser::getId, u -> u));

		Map<String, HaveStock> haveStockMap = haveStockRepository
				.findByUserIdsAndStockCode(userIds, event.getStockCode()).stream()
				.collect(Collectors.toMap(h -> h.getStockUser().getId(), h -> h));

		applyStockChanges(event, userMap, haveStockMap);
		sendUpdates(event, userMap, haveStockMap);
	}

	public void applyStockChanges(SettlementEvent settlementevent, Map<String, StockUser> userMap,
	        Map<String, HaveStock> haveStockMap) {

		if (settlementevent.getStockChanges().isEmpty())
	        return;

		for (haveStockChange change : settlementevent.getStockChanges()) {
	        StockUser user = userMap.get(change.getUserId());

	        if (user == null) {
	            log.warn("정산 대상 사용자 없음: {}", change.getUserId());
	            continue;
	        }

			int tradeMoney = change.getTradePrice() * Math.abs(change.getTradeQuantity());

			if (change.getTradeQuantity() > 0) {
	            user.setAsset(user.getAsset() - tradeMoney);
			} else {
				user.setAsset(user.getAsset() + tradeMoney);
				user.setAvailableAsset(user.getAvailableAsset() + tradeMoney);
			}

			HaveStock hs = haveStockMap.computeIfAbsent(change.getUserId(), k -> {
				HaveStock h = new HaveStock();
				h.setStockUser(user);
				h.setStockCode(settlementevent.getStockCode());
				h.setQuantity(0);
				h.setAvailableQuantity(0);
				h.setAveragePrice(0);
				return h;
			});

			if (change.getTradeQuantity() > 0) {
				hs.updateAveragePrice(change.getTradeQuantity(), change.getTradePrice());
				hs.setAvailableQuantity(hs.getAvailableQuantity() + change.getTradeQuantity()
	            );
	        } else {
				hs.setQuantity(hs.getQuantity() + change.getTradeQuantity()
	            );
	        }
	    }

		stockUserRepository.saveAll(userMap.values());
		List<HaveStock> deleteStocks = haveStockMap.values()
	            .stream()
				.filter(hs -> hs.getQuantity() == 0)
	            .toList();

		if (!deleteStocks.isEmpty()) {
			haveStockRepository.deleteAll(deleteStocks);
	    }

		haveStockRepository.saveAll(haveStockMap.values().stream().filter(hs -> hs.getQuantity() != 0).toList());
	}

	public void sendUpdates(SettlementEvent event, Map<String, StockUser> userMap,
			Map<String, HaveStock> haveStockMap) {
		for (haveStockChange change : event.getStockChanges()) {
			StockUser user = userMap.get(change.getUserId());
			if (user != null)
				userWebsocketService.sendUserAsset(user);

			HaveStock hs = haveStockMap.get(change.getUserId());
			userWebsocketService.sendUserStock(change.getUserId(), hs, event.getStockCode());
		}
	}
}