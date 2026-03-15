package Poi.Stock.DTO.user;

import lombok.Getter;

@Getter
public class ApiResponse {
	private boolean success;
	private String message;
	private Object data;

	public ApiResponse(boolean success, String message, Object data) {
		super();
		this.success = success;
		this.message = message;
		this.data = data;
	}

	public ApiResponse(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}

}
