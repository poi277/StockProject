// SettlementConsumer.java
package Poi.Stock.features.Kafka;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.features.UserWebsocket.UserWebsocketService;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.shared.event.SettlementEvent;
import Poi.Stock.shared.event.SettlementEvent.AssetChange;
import Poi.Stock.shared.event.SettlementEvent.haveStockChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementConsumer {

	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;
	private final UserWebsocketService userWebsocketService;

	@KafkaListener(topics = "settlement-topic", groupId = "settlement-group")
	@Transactional
	public void consume(@Payload SettlementEvent event) {
		log.info("정산 이벤트 수신: {}", event); // ✅ 1. 메시지 수신 확인
		try {
			log.info("assetChanges: {}", event.getAssetChanges()); // ✅ 2. 자산 변경 확인
			log.info("stockChanges: {}", event.getStockChanges()); // ✅ 3. 주식 변경 확인

			List<String> userIds = event.getAssetChanges().stream().map(AssetChange::getUserId).toList();
			List<String> stockUserIds = event.getStockChanges().stream().map(haveStockChange::getUserId).toList();
			log.info("userIds: {}, stockUserIds: {}", userIds, stockUserIds); // ✅ 4. 유저 ID 확인

			Map<String, StockUser> userMap = stockUserRepository.findAllById(userIds).stream()
					.collect(Collectors.toMap(StockUser::getId, u -> u));
			log.info("userMap: {}", userMap.keySet()); // ✅ 5. 유저 조회 확인

			Map<String, HaveStock> haveStockMap = haveStockRepository
					.findByUserIdsAndStockCode(stockUserIds, event.getStockCode()).stream()
					.collect(Collectors.toMap(h -> h.getStockUser().getId(), h -> h));
			log.info("haveStockMap: {}", haveStockMap.keySet()); // ✅ 6. 보유주식 조회 확인

			applyAssetChanges(event.getAssetChanges(), userMap);
			applyStockChanges(event.getStockChanges(), event.getStockCode(), userMap, haveStockMap);
			sendUpdates(event, userMap, haveStockMap);

		} catch (Exception e) {
			log.error("정산 처리 실패: {}", e.getMessage(), e);
			throw e;
		}
	}

	private void sendUpdates(SettlementEvent event, Map<String, StockUser> userMap,
			Map<String, HaveStock> haveStockMap) {
		for (AssetChange change : event.getAssetChanges()) {
			StockUser user = userMap.get(change.getUserId());
			if (user != null) {
				userWebsocketService.sendUserAsset(user);
			}
		}
		for (haveStockChange change : event.getStockChanges()) {
			HaveStock hs = haveStockMap.get(change.getUserId());
			userWebsocketService.sendUserStock(change.getUserId(), hs, event.getStockCode());
		}
	}

	private void applyAssetChanges(List<AssetChange> changes, Map<String, StockUser> userMap) {
		if (changes.isEmpty())
			return;
		for (AssetChange change : changes) {
			StockUser user = userMap.get(change.getUserId());
			if (user == null) {
				log.warn("정산 대상 사용자 없음: {}", change.getUserId());
				continue;
			}
			user.setAsset(user.getAsset() + change.getTradeMoney());
			if (change.getTradeMoney() > 0) {
				user.setAvailableAsset(user.getAvailableAsset() + change.getTradeMoney());
			}
		}
		stockUserRepository.saveAll(userMap.values());
	}

	private void applyStockChanges(List<haveStockChange> changes, String stockCode, Map<String, StockUser> userMap,
			Map<String, HaveStock> haveStockMap) {
		if (changes.isEmpty())
			return;
		for (haveStockChange change : changes) {
			HaveStock hs = haveStockMap.computeIfAbsent(change.getUserId(), k -> {
				HaveStock h = new HaveStock();
				h.setStockUser(userMap.get(k));
				h.setStockCode(stockCode);
				h.setQuantity(0);
				h.setAvailableQuantity(0);
				h.setAveragePrice(0);
				return h;
			});
			if (change.getTradeQuantity() > 0) {
				updateAveragePrice(hs, change.getTradeQuantity(), change.getTradePrice());
				hs.setAvailableQuantity(hs.getAvailableQuantity() + change.getTradeQuantity());
			} else {
				hs.setQuantity(hs.getQuantity() + change.getTradeQuantity());
			}
		}
		haveStockRepository.saveAll(haveStockMap.values());
	}

	private void updateAveragePrice(HaveStock hs, int qty, int price) {
		if (hs.getQuantity() == 0) {
			hs.setAveragePrice(price);
		} else {
			double total = hs.getAveragePrice() * hs.getQuantity() + (double) price * qty;
			hs.setAveragePrice(total / (hs.getQuantity() + qty));
		}
		hs.setQuantity(hs.getQuantity() + qty);
	}
}