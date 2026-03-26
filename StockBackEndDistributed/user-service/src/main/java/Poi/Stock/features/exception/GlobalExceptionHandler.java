package Poi.Stock.features.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Map 말고 String으로
	}

//	@ExceptionHandler(RuntimeException.class)
//	public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
//		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
//	}
}