package Poi.Stock.object;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeExecutionList {
	private List<TradeExecution> executions;
}
