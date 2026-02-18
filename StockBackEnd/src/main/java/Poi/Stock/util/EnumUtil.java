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
}
