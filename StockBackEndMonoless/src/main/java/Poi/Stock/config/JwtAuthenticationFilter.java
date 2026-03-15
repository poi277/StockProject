package Poi.Stock.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String token = resolveToken(req);

		if (StringUtils.hasText(token) && jwtProvider.validate(token)
				&& "access".equals(jwtProvider.getTokenType(token))) {

			// authentication.getName() → userId (String) 기존과 동일
			String userId = jwtProvider.getUserId(token);
			var auth = new UsernamePasswordAuthenticationToken(userId, null,
					List.of(new SimpleGrantedAuthority("ROLE_USER")));
			SecurityContextHolder.getContext().setAuthentication(auth);
		}

		chain.doFilter(req, res);
	}

	private String resolveToken(HttpServletRequest req) {
		String bearer = req.getHeader("Authorization");
		if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		return null;
	}
}