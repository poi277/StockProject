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
		BULL, // 상승장 - 완전 정배열 (MA5 > MA20 > MA60)
		BEAR, // 하락장 - 완전 역배열 (MA5 < MA20 < MA60)
		FLAT // 횡보장 - 그 외
	}

	public enum BotType {
		INSTITUTION, // 기관
		FOREIGN, // 외국인
		INDIVIDUAL, // 개인
	}

	public enum FailStatus {
		PENDING_REVIEW, PERMANENT_FAIL
	}

	public enum TradeDecision {
		BUY, SELL, HOLD, CANCEL
	}

	public enum CandleType {
		ONE_MINUTE(1, 0), THREE_MINUTE(3, 0), FIVE_MINUTE(5, 0), TEN_MINUTE(10, 0), HOUR(0, 1), TWO_HOUR(0, 2),
		THREE_HOUR(0, 3), FOUR_HOUR(0, 4), DAY(0, 0), WEEK(0, 0), MONTH(0, 0), YEAR(0, 0);
		private final int minute;
		private final int hourGroup;

		CandleType(int minute, int hourGroup) {
			this.minute = minute;
			this.hourGroup = hourGroup;
		}

		public int getMinute() {
			return minute;
		}

		public int getHourGroup() {
			return hourGroup;
		}

		public boolean isMinuteType() {
			return minute > 0;
		}

		public boolean isHourType() {
			return hourGroup > 0;
		}
	}

}
