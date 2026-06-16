package Poi.Stock.features.Bot;

import Poi.Stock.util.EnumUtil.BotType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BotList {

	// 🎯 여기에 한 줄만 추가하면 DB에 들어갈 봇 계정이 정의됩니다!
	INSTITUTION_1("BOT_INSTITUTION", BotType.INSTITUTION), FOREIGN_1("BOT_FOREIGN", BotType.FOREIGN),
	INDIVIDUAL_1("BOT_INDIVIDUAL_1", BotType.INDIVIDUAL), INDIVIDUAL_2("BOT_INDIVIDUAL_2", BotType.INDIVIDUAL),
	INDIVIDUAL_3("BOT_INDIVIDUAL_3", BotType.INDIVIDUAL), INDIVIDUAL_4("BOT_INDIVIDUAL_4", BotType.INDIVIDUAL),
	INDIVIDUAL_5("BOT_INDIVIDUAL_5", BotType.INDIVIDUAL), INDIVIDUAL_6("BOT_INDIVIDUAL_6", BotType.INDIVIDUAL);

	private final String botId;
	private final BotType botType;
}