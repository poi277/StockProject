package Poi.Stock.object;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Poi.Stock.features.Order.Order;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MatchingResult {
	private String stockCode;
	private Set<Integer> matchedPrices;
	private List<TradeExecutionList> executions;
	private List<Order> completedResting;
	private List<Order> partialResting;
	private Order incomingOrder;

	// 초기화 생성자
	public MatchingResult(String stockCode) {
		this.matchedPrices = new HashSet<>();
		this.executions = new ArrayList<>();
		this.completedResting = new ArrayList<>();
		this.partialResting = new ArrayList<>();
		this.stockCode = stockCode;
	}

	public Integer getLastExecutionPrice() {
		if (executions.isEmpty())
			return null;
		return executions.get(executions.size() - 1).getPrice();
	}

	public int getTotalFilledQty() {
		return executions.stream().mapToInt(TradeExecutionList::getQuantity).sum();
	}

	public LocalDateTime getLastExecutionTime() {
		if (executions.isEmpty())
			return null;
		return executions.get(executions.size() - 1).getTime();
	}

	public int getBuyFilledQty() {
		return executions.stream().filter(e -> e.getTradeType() == tradeType.BUY).mapToInt(TradeExecutionList::getQuantity)
				.sum();
	}

	public int getSellFilledQty() {
		return executions.stream().filter(e -> e.getTradeType() == tradeType.SELL).mapToInt(TradeExecutionList::getQuantity)
				.sum();
	}

	public long getTotalTradeAmount() {
		return executions.stream().mapToLong(e -> (long) e.getPrice() * e.getQuantity()).sum();
	}
}
