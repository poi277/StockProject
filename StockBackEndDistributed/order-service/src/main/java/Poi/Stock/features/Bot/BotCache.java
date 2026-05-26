package Poi.Stock.features.Bot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class BotCache {

	private final Map<String, Bot> bots = new HashMap<>();

	public void register(Bot bot) {
		bots.put(bot.getBotId(), bot);
	}

	public Bot get(String botId) {
		return bots.get(botId);
	}

	public Map<String, Bot> getAll() {
		return bots;
	}

	public boolean isBot(String botId) {
		Bot bot = bots.get(botId);
		return bot != null && bot.getBotType() != null;
	}
}