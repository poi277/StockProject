package Poi.Stock.DTO.user;

import Poi.Stock.features.User.StockUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class getHaveStockDTO {

	private Long id;
	private StockUser stockUser;
	private String stockCode;
	private Integer quantity;
	private Integer availableQuantity;
	private double averagePrice;
}
