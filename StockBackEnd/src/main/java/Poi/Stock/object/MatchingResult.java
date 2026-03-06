package Poi.Stock.object;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MatchingResult {
	private Set<Integer> matchedPrices;
	private List<TradeExecution> executions;

}
