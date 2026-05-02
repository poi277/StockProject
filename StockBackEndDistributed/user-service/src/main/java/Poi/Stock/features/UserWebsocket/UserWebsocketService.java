// UserWebsocketService.java
package Poi.Stock.features.UserWebsocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWebsocketService {

	private final SimpMessagingTemplate messagingTemplate;

	public void sendUserAsset(StockUser user) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("asset", user.getAsset());
		payload.put("availableAsset", user.getAvailableAsset());

		log.info("자산 전송 userId={}, asset={}", user.getId(), user.getAsset());
		messagingTemplate.convertAndSendToUser(user.getId(), "/queue/asset", payload);
	}

	public void sendUserStock(String userId, HaveStock hs, String stockCode) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("stockCode", stockCode);

		if (hs != null && hs.getQuantity() > 0) {
			payload.put("id", hs.getId());
			payload.put("quantity", hs.getQuantity());
			payload.put("availableQuantity", hs.getAvailableQuantity());
			payload.put("averagePrice", hs.getAveragePrice());
		} else {
			payload.put("quantity", 0);
			payload.put("availableQuantity", 0);
			payload.put("averagePrice", 0);
		}

		log.info("보유주식 전송 userId={}, stockCode={}", userId, stockCode);
		messagingTemplate.convertAndSendToUser(userId, "/queue/havestock", payload);
	}
}