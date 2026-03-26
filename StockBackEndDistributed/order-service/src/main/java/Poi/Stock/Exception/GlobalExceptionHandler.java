package Poi.Stock.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import Poi.Stock.DTO.user.ApiResponse;
@RestControllerAdvice
public class GlobalExceptionHandler {

	// @Valid 검증 실패
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> handleValidException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage) // 메시지만
				.findFirst().orElse("입력값이 유효하지 않습니다.");
		return ResponseEntity.badRequest().body(new ApiResponse(false, message));
	}

	// 일반 예외
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException e) {
		return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
	}
}