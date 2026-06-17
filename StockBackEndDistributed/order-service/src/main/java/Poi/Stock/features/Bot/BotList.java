package Poi.Stock.features.Bot;

import Poi.Stock.util.EnumUtil.BotType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BotList {

	// 🎯 여기에 한 줄만 추가하면 DB에 들어갈 봇 계정이 정의됩니다!
	INSTITUTION_1("BOT_INSTITUTION", BotType.INSTITUTION, 30), FOREIGN_1("BOT_FOREIGN", BotType.FOREIGN, 30),
	FOREIGN_2("BOT_FOREIGN2", BotType.FOREIGN, 50), FOREIGN_3("BOT_FOREIGN3", BotType.FOREIGN, 70),
	INDIVIDUAL_1("BOT_INDIVIDUAL_1", BotType.INDIVIDUAL, 70), INDIVIDUAL_2("BOT_INDIVIDUAL_2", BotType.INDIVIDUAL, 60),
	INDIVIDUAL_3("BOT_INDIVIDUAL_3", BotType.INDIVIDUAL, 50), INDIVIDUAL_4("BOT_INDIVIDUAL_4", BotType.INDIVIDUAL, 80);

	private final String botId;
	private final BotType botType;
	private final int botBaseIntensity;
}