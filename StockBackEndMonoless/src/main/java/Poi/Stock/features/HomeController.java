package Poi.Stock.features;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
	@GetMapping("/hello")
	public ResponseEntity<Map<String, String>> hello() {
		Map<String, String> response = Map.of("message", "hello");
		return ResponseEntity.ok(response); // 200 OK
	}
}
