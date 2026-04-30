package Poi.Stock.object;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Poi.Stock.features.Order.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MatchingResult {
	private Set<Integer> matchedPrices;
	private List<TradeExecution> executions;
	private List<Order> completedResting;
	private List<Order> partialResting;
	private Order incomingOrder;

	// 초기화 생성자
	public MatchingResult() {
		this.matchedPrices = new HashSet<>();
		this.executions = new ArrayList<>();
		this.completedResting = new ArrayList<>();
		this.partialResting = new ArrayList<>();
	}

	public Integer getLastExecutionPrice() {
		if (executions.isEmpty())
			return null;
		return executions.get(executions.size() - 1).getPrice();
	}

	public int getTotalFilledQty() {
		return executions.stream().mapToInt(TradeExecution::getQuantity).sum();
	}

	public LocalDateTime getLastExecutionTime() {
		if (executions.isEmpty())
			return null;
		return executions.get(executions.size() - 1).getTime();
	}

	public Double getLastChangeRate() {
		if (executions.isEmpty())
			return null;
		return executions.get(executions.size() - 1).getChangeRate();
	}
}
