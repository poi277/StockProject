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
		log.info("정산 이벤트 수신: {}", event);
		try {
			List<String> userIds = event.getStockChanges().stream().map(haveStockChange::getUserId).toList();

			Map<String, StockUser> userMap = stockUserRepository.findAllById(userIds).stream()
					.collect(Collectors.toMap(StockUser::getId, u -> u));

			Map<String, HaveStock> haveStockMap = haveStockRepository
					.findByUserIdsAndStockCode(userIds, event.getStockCode()).stream()
					.collect(Collectors.toMap(h -> h.getStockUser().getId(), h -> h));

			applyStockChanges(event, userMap, haveStockMap);
			sendUpdates(event, userMap, haveStockMap);

		} catch (Exception e) {
			log.error("정산 처리 실패: {}", e.getMessage(), e);
			throw e;
		}
	}

	private void applyStockChanges(SettlementEvent settlementevent, Map<String, StockUser> userMap,
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
				hs.setAvailableQuantity(hs.getAvailableQuantity() + change.getTradeQuantity());
			} else {
				hs.setQuantity(hs.getQuantity() + change.getTradeQuantity());
			}
		}
		stockUserRepository.saveAll(userMap.values());
		haveStockRepository.saveAll(haveStockMap.values());
	}

	private void sendUpdates(SettlementEvent event, Map<String, StockUser> userMap,
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