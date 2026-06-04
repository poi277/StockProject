package Poi.Stock.util;

public class EnumUtil {
	public enum tradeType {
		BUY, SELL
	}
	public enum OrderStatus {
		PENDING, // 대기 중
		PARTIAL, // 부분 체결
		COMPLETED, // 전량 체결
		CANCELLED // 취소됨
	}

	public enum MarketState {
		BULL, // 상승장 - 매수 우세, 가격 지속 상승
		BEAR, // 하락장 - 매도 우세, 가격 지속 하락
	}

	public enum BotType {
		INSTITUTION, // 기관
		FOREIGN, // 외국인
		INDIVIDUAL, // 개인
	}

	public enum FailStatus {
		PENDING_REVIEW, PERMANENT_FAIL
	}

	public enum CandleType {
		ONE_MINUTE(1), THREE_MINUTE(3), FIVE_MINUTE(5), TEN_MINUTE(10),
		HOUR(0), DAY(0), WEEK(0), MONTH(0), YEAR(0);

		private final int minute;

		CandleType(int minute) {
			this.minute = minute;
		}

		public int getMinute() {
			return minute;
		}

		public boolean isMinuteType() {
			return minute > 0;
		}
	}

}
