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
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.shared.event.SettlementEvent;
import Poi.Stock.shared.event.SettlementEvent.AssetChange;
import Poi.Stock.shared.event.SettlementEvent.StockChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementConsumer {

	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;

	@KafkaListener(topics = "settlement-topic", groupId = "settlement-group")
	@Transactional
	public void consume(@Payload SettlementEvent event) {
		try {
			applyAssetChanges(event.getAssetChanges());
			applyStockChanges(event.getStockChanges(), event.getStockCode());
			log.info("정산 완료: stockCode={}, 자산={}건, 주식={}건", event.getStockCode(), event.getAssetChanges().size(),
					event.getStockChanges().size());
		} catch (Exception e) {
			log.error("정산 처리 실패: {}", e.getMessage(), e);
			throw e; // Kafka retry 트리거
		}
	}

	private void applyAssetChanges(List<AssetChange> changes) {
		if (changes.isEmpty())
			return;
		List<String> userIds = changes.stream().map(AssetChange::getUserId).toList();
		Map<String, StockUser> userMap = stockUserRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(StockUser::getId, u -> u));
		for (AssetChange change : changes) {
			StockUser user = userMap.get(change.getUserId());
			if (user == null) {
				log.warn("정산 대상 사용자 없음: {}", change.getUserId());
				continue;
			}
			user.setAsset(user.getAsset() + change.getDelta());
			if (change.getDelta() > 0) {
				// 매도 체결: availableAsset도 증가
				user.setAvailableAsset(user.getAvailableAsset() + change.getDelta());
			}
			// 매수 체결(delta < 0): asset은 위에서 차감됨, availableAsset은 주문 시 이미 차감됐으므로 건드리지 않음
		}
		stockUserRepository.saveAll(userMap.values());
	}

	private void applyStockChanges(List<StockChange> changes, String stockCode) {
		if (changes.isEmpty())
			return;
		List<String> userIds = changes.stream().map(StockChange::getUserId).toList();
		Map<String, StockUser> userMap = stockUserRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(StockUser::getId, u -> u));
		Map<String, HaveStock> haveStockMap = haveStockRepository.findByUserIdsAndStockCode(userIds, stockCode).stream()
				.collect(Collectors.toMap(h -> h.getStockUser().getId(), h -> h));
		for (StockChange change : changes) {
			HaveStock hs = haveStockMap.computeIfAbsent(change.getUserId(), k -> {
				HaveStock h = new HaveStock();
				h.setStockUser(userMap.get(k));
				h.setStockCode(stockCode);
				h.setQuantity(0);
				h.setAvailableQuantity(0);
				h.setAveragePrice(0);
				return h;
			});
			if (change.getQuantityDelta() > 0) {
				// 매수 체결: quantity + availableQuantity 둘 다 증가
				updateAveragePrice(hs, change.getQuantityDelta(), change.getFillPrice());
				hs.setAvailableQuantity(hs.getAvailableQuantity() + change.getQuantityDelta());
			} else {
				// 매도 체결: quantity만 감소 (availableQuantity는 주문 시 이미 차감)
				hs.setQuantity(hs.getQuantity() + change.getQuantityDelta());
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