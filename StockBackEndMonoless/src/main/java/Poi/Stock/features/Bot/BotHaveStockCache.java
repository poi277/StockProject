package Poi.Stock.features.Bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class BotHaveStockCache {

	// botId → (stockCode → BotHaveStock)
	private final Map<String, Map<String, BotHaveStock>> botHaveStock = new HashMap<>();

	public void register(String botId, List<BotHaveStock> stocks) {
		Map<String, BotHaveStock> stockMap = new HashMap<>();
		stocks.forEach(s -> stockMap.put(s.getStockCode(), s));
		botHaveStock.put(botId, stockMap);
	}

	public BotHaveStock get(String botId, String stockCode) {
		Map<String, BotHaveStock> stockMap = botHaveStock.get(botId);
		if (stockMap == null)
			return null;
		return stockMap.get(stockCode);
	}

	public Map<String, BotHaveStock> getAll(String botId) {
		return botHaveStock.getOrDefault(botId, new HashMap<>());
	}
}