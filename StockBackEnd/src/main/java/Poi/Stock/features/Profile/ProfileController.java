package Poi.Stock.features.Profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import Poi.Stock.DTO.user.ProfileDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

	private ProfileService profileService;
	@GetMapping("/")
	public ResponseEntity<ApiResponse> getprofile(Authentication authentication) {
		String userId = authentication.getName();
		ProfileDTO userProfile = profileService.getProfile(userId);
		return ResponseEntity.ok(new ApiResponse(true, "프로필 불러오기 완료", userProfile));
	}
}
