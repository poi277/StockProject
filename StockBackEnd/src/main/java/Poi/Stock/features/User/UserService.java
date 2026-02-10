package Poi.Stock.features.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.UserRegisterDto;
import Poi.Stock.repository.StockUserRepository;

@Service
public class UserService {

	private final PasswordEncoder passwordEncoder;
	private final StockUserRepository userRepository;

	public UserService(PasswordEncoder passwordEncoder, StockUserRepository userRepository) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	public void registerUser(UserRegisterDto dto) {
		try {
			if (userRepository.existsById(dto.getId())) {
				throw new IllegalArgumentException("이미 존재하는 사용자입니다");
			}
			StockUser user = new StockUser();
			user.setId(dto.getId());
			user.setUsername(dto.getUsername());
			user.setPassword(passwordEncoder.encode(dto.getPassword()));
			userRepository.save(user);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다", e);
		}
	}
}