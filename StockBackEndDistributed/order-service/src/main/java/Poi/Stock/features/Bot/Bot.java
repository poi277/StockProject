package Poi.Stock.features.Bot;

import Poi.Stock.util.EnumUtil.BotType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class Bot {
	@Id
	private String botId;
	@Enumerated(EnumType.STRING)
	private BotType botType;
	private long asset;

	public boolean isBot() {
		return botType != null;
	}
}