package Poi.Stock.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CandleDTO {
	private String time;
	private int open;
	private int high;
	private int low;
	private int close;
	private long totalVolume;
}