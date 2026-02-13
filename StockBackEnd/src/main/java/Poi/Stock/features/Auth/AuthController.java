package Poi.Stock.features.Auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	public AuthController(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			// 1. 인증
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getId(), loginDTO.getPassword()));
			// 2. SecurityContext 생성 및 설정
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(authentication);
			SecurityContextHolder.setContext(context);
			// 3. 세션에 SecurityContext 저장
			securityContextRepository.saveContext(context, request, response);
			// 4. 세션 ID 확인 (디버깅용)
			HttpSession session = request.getSession(false);
			if (session != null) {
				System.out.println("✅ 세션 ID: " + session.getId());
				System.out.println("✅ 로그인 사용자: " + authentication.getName());
			}
			// 5. 사용자 ID를 data에 담아서 반환
			String userId = authentication.getName();
			return ResponseEntity.ok(new ApiResponse(true, "로그인 성공", userId));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ApiResponse(false, "로그인 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/check")
	public ResponseEntity<ApiResponse> checkAuth(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()) {
			// 인증된 사용자의 ID를 data에 담아서 반환
			String userId = authentication.getName();
			return ResponseEntity.ok(new ApiResponse(true, "인증됨", userId));
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ApiResponse(false, "인증되지 않음"));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.ok(new ApiResponse(true, "로그아웃 성공"));
	}
}

