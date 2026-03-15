package Poi.Stock.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {

	@Value("${jwt.secret}")
	private String secretStr;

	@Value("${jwt.access-expiration}")
	private long accessExp;

	@Value("${jwt.refresh-expiration}")
	private long refreshExp;

	private SecretKey key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(secretStr.getBytes(StandardCharsets.UTF_8));
	}

	/** 기존 모놀리스와 동일: subject = userId (String) */
	public String createAccessToken(String userId) {
		return Jwts.builder().subject(userId).claim("type", "access").issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + accessExp)).signWith(key).compact();
	}

	public String createRefreshToken(String userId) {
		return Jwts.builder().subject(userId).claim("type", "refresh").issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + refreshExp)).signWith(key).compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	public boolean validate(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.warn("만료된 토큰");
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("유효하지 않은 토큰: {}", e.getMessage());
		}
		return false;
	}

	/** authentication.getName() 과 동일한 값 반환 */
	public String getUserId(String token) {
		return parseClaims(token).getSubject();
	}

	public String getTokenType(String token) {
		return (String) parseClaims(token).get("type");
	}
}