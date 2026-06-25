package Poi.Stock.features.Candle.Entity;

import java.util.Map;

public interface Candle {

	String getStockCode();

	String getCandleTime();

	void setCandleTime(String string);

	Integer getOpen();

	void setOpen(Integer open);

	Integer getHigh();

	void setHigh(Integer high);

	Integer getLow();

	void setLow(Integer low);

	Integer getClose();

	void setClose(Integer close);

	Long getBuyQty();

	void setBuyQty(Long buyQty);

	Long getSellQty();

	void setSellQty(Long sellQty);

	Long getTotalVolume();

	void setTotalVolume(Long totalVolume);

	Long getTradeAmount();

	void setTradeAmount(Long tradeAmount);

	static <T extends Candle> T fromRedisMap(Map<Object, Object> redisMap, CandleFactory<T> factory) {
		try {
			// 🎯 아래 내부 헬퍼 메서드(parseInt / parseLong)를 거치므로 Object 에러가 안 납니다.
			int open = parseInt(redisMap.get("open"));
			int high = parseInt(redisMap.get("high"));
			int low = parseInt(redisMap.get("low"));
			int close = parseInt(redisMap.get("close"));
			long buyQty = parseLong(redisMap.get("buyQty"));
			long sellQty = parseLong(redisMap.get("sellQty"));
			long tradeAmount = parseLong(redisMap.get("tradeAmount"));
			long totalVolume = buyQty + sellQty;

			return factory.create(open, high, low, close, buyQty, sellQty, totalVolume, tradeAmount);
		} catch (Exception e) {
			throw new RuntimeException("Redis 캔들 데이터 파싱 및 변환 실패", e);
		}
	}

	// 🎯 [핵심 수정] Object 타입을 문자열로 안전하게 바꾼 뒤 primitive type으로 파싱합니다.
	private static int parseInt(Object val) {
		if (val == null)
			return 0;
		String str = String.valueOf(val).trim(); // Object를 명시적으로 String 변환
		return str.isEmpty() ? 0 : Integer.parseInt(str);
	}

	private static long parseLong(Object val) {
		if (val == null)
			return 0L;
		String str = String.valueOf(val).trim(); // Object를 명시적으로 String 변환
		return str.isEmpty() ? 0L : Long.parseLong(str);
	}

	@FunctionalInterface
	interface CandleFactory<T> {
		T create(int open, int high, int low, int close, long buyQty, long sellQty, long totalVolume, long tradeAmount);
	}

}