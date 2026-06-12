package Poi.Stock.features.Bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class BotCache {

	private final Map<String, Bot> bots = new HashMap<>();

	// 스케줄러와 Init이 공유할봇 객체 저장소
	@Getter
	private final List<AbstractBot> runningBotsCache = new ArrayList<>();

	public void register(Bot bot) {
		bots.put(bot.getBotId(), bot);
	}
	public void registerInstance(AbstractBot botInstance) {
		this.runningBotsCache.add(botInstance);
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