package Poi.Stock.config.kis;

import lombok.Data;

@Data
public class CurrentPriceResponse {

	private String stck_prpr;
	private String stck_oprc;
	private String stck_hgpr;
	private String stck_lwpr;
	private String prdy_vrss;
	private String prdy_ctrt;

}