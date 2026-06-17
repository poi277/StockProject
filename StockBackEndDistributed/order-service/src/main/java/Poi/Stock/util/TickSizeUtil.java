package Poi.Stock.util;

public class TickSizeUtil {

	private TickSizeUtil() {
	}

	public static int getTickSize(int price) {
		if (price < 1000)
			return 1;
		if (price < 5000)
			return 5;
		if (price < 10000)
			return 10;
		if (price < 50000)
			return 50;
		if (price < 100000)
			return 100;
		if (price < 500000)
			return 500;
		return 1000;
	}

	public static int addTicks(int price, int tickCount) {
		int result = price;
		for (int i = 0; i < tickCount; i++) {
			result += getTickSize(result);
		}
		return result;
	}

	public static int subtractTicks(int price, int tickCount) {
		int result = price;
		for (int i = 0; i < tickCount; i++) {
			result -= getTickSize(Math.max(1, result - 1));
		}
		return Math.max(1, result);
	}

	private static int tick(int price) {
		return getTickSize(price);
	}
}