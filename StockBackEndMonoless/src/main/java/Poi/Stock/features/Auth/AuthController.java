package Poi.Stock.features.Auth;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.LoginDTO;
import Poi.Stock.config.JwtProvider;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	private final RedisTemplate<String, String> redisTemplate;

	private static final String REFRESH_PREFIX = "refresh:";

	@PostMapping("/login")
	public ResponseEntity<ApiResponse> login(@RequestBody LoginDTO loginDTO) {
		try {
			Authentication auth = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getId(), loginDTO.getPassword()));

			String userId = auth.getName(); // 기존과 동일: String id
			String accessToken = jwtProvider.createAccessToken(userId);
			String refreshToken = jwtProvider.createRefreshToken(userId);

			// Redis에 refresh token 저장 (7일)
			redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, refreshToken, 7, TimeUnit.DAYS);

			return ResponseEntity.ok(new ApiResponse(true, "로그인 성공",
					Map.of("accessToken", accessToken, "refreshToken", refreshToken, "userId", userId)));

		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ApiResponse(false, "로그인 실패: " + e.getMessage()));
		}
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse> refresh(@RequestBody Map<String, String> body) {
		String refreshToken = body.get("refreshToken");

		if (!jwtProvider.validate(refreshToken) || !"refresh".equals(jwtProvider.getTokenType(refreshToken))) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "유효하지 않은 refresh token"));
		}

		String userId = jwtProvider.getUserId(refreshToken);
		String saved = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);

		if (!refreshToken.equals(saved)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "토큰이 일치하지 않습니다"));
		}

		String newAccess = jwtProvider.createAccessToken(userId);
		String newRefresh = jwtProvider.createRefreshToken(userId);
		redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, newRefresh, 7, TimeUnit.DAYS);

		return ResponseEntity
				.ok(new ApiResponse(true, "토큰 갱신 성공", Map.of("accessToken", newAccess, "refreshToken", newRefresh)));
	}

	// 기존 /auth/check 그대로 유지
	@GetMapping("/check")
	public ResponseEntity<ApiResponse> checkAuth(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()) {
			return ResponseEntity.ok(new ApiResponse(true, "인증됨", authentication.getName()));
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "인증되지 않음"));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse> logout(Authentication authentication) {
		if (authentication != null) {
			redisTemplate.delete(REFRESH_PREFIX + authentication.getName());
		}
		return ResponseEntity.ok(new ApiResponse(true, "로그아웃 성공"));
	}
}