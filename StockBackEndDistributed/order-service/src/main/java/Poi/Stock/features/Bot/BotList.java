package Poi.Stock.features.Bot;

import Poi.Stock.util.EnumUtil.BotType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BotList {

	// 🎯 여기에 한 줄만 추가하면 DB에 들어갈 봇 계정이 정의됩니다!
	INSTITUTION_1("BOT_INSTITUTION", BotType.INSTITUTION), FOREIGN_1("BOT_FOREIGN", BotType.FOREIGN),

	// 개인 봇들을 원하는 만큼 명단에 넣습니다.
	INDIVIDUAL_1("BOT_INDIVIDUAL_1", BotType.INDIVIDUAL), INDIVIDUAL_2("BOT_INDIVIDUAL_2", BotType.INDIVIDUAL),
	INDIVIDUAL_3("BOT_INDIVIDUAL_3", BotType.INDIVIDUAL);

	private final String botId;
	private final BotType botType;
}