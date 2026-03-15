package Poi.Stock.config.kis;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KisTokenService {

	private final KisConfig kisConfig;
	private final WebClient webClient;

	private String accessToken;
	private LocalDateTime tokenExpiredAt;
	public KisTokenService(KisConfig kisConfig) {
		this.kisConfig = kisConfig;
		this.webClient = WebClient.builder().baseUrl(kisConfig.getBaseUrl()).build();
	}

	public String getAccessToken() {
		if (accessToken == null || LocalDateTime.now().isAfter(tokenExpiredAt)) {
			issueToken();
		}
		return accessToken;
	}

	private void issueToken() {
		Map<String, String> body = new HashMap<>();
		body.put("grant_type", "client_credentials");
		body.put("appkey", kisConfig.getAppkey());
		body.put("appsecret", kisConfig.getAppsecret());
		Map<String, Object> response = webClient.post().uri("/oauth2/tokenP").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body).retrieve().bodyToMono(Map.class).block();
		this.accessToken = (String) response.get("access_token");
		this.tokenExpiredAt = LocalDateTime.now().plusHours(23);

		log.info("KIS AccessToken 발급 완료");
	}

}