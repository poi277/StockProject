package Poi.Stock.object;

import java.time.LocalDateTime;

import Poi.Stock.util.EnumUtil.tradeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TradeExecutionList {
	private tradeType tradeType;
	private String buyerId;
	private String sellerId;
	private int quantity; // 체결량
	private int price; // 체결가
	private String stockCode;
	private LocalDateTime time; // 시간
}