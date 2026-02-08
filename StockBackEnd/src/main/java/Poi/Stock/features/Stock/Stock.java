package Poi.Stock.features.Stock;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
	@Id
	private String id; // 종목코드
	private String name; // 종목명 추가
	private Integer price; // 현재가
	private Integer changeAmount; // 전일대비 금액
	private Double changeRate; // 전일대비 등락률
	private Long volume; // 거래량
}