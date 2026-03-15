package Poi.Stock.config;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.StockUserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final StockUserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		StockUser user = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

		// 기존 모놀리스와 동일: username = id (String)
		return new org.springframework.security.core.userdetails.User(user.getId(), user.getPassword(),
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
	}
}