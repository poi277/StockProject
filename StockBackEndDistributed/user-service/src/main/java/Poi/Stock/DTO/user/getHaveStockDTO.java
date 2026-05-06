package Poi.Stock.DTO.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class getHaveStockDTO {

	private Long id;
	private String stockCode;
	private Integer quantity;
	private Integer availableQuantity;
	private Integer averagePrice;
}