package Poi.Stock.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.StockUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final StockUserRepository stockUserRepository;

	public CustomUserDetailsService(StockUserRepository stockUserRepository) {
		this.stockUserRepository = stockUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		StockUser user = stockUserRepository.findById(username)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

		return User.builder().username(user.getId()) // 로그인 ID
				.password(user.getPassword()) // DB에 저장된 BCrypt
				.roles("USER") // 나중에 role 컬럼 추가 가능
				.build();
	}
}
