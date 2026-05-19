package Poi.Stock.DTO.user;

import Poi.Stock.util.EnumUtil.tradeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TradeDTO {
	private String userId;
	private Long orderId;
	private String StockName;
	@NotBlank(message = "종목 코드는 필수입니다")
	private String stockCode;

	@NotNull(message = "가격은 필수입니다")
	@Positive(message = "가격은 0보다 커야 합니다")
	private Integer tradePrice;

	@NotNull(message = "수량은 필수입니다")
	@Positive(message = "수량은 0보다 커야 합니다")
	private Integer quantity;

	@NotNull(message = "거래 유형은 필수입니다")
	private tradeType tradeType;

	public tradeType getTradeType() {
		return tradeType;
	}

	public void setTradeType(tradeType tradeType) {
		this.tradeType = tradeType;
	}

}

