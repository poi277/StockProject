package Poi.Stock.object;

import java.time.LocalDateTime;

import Poi.Stock.util.EnumUtil.tradeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeExecution {
	private tradeType tradeType;
	private String buyerId;
	private String sellerId;
	private int quantity; // 체결량
	private int price; // 체결가
	private String stockCode;
	private double changeRate; // 등락률
	private long totalVolume; // 주식의 총거래량
	private LocalDateTime time; // 시간
}