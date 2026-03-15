package Poi.Stock.config.kis;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KisStockService {

	private final KisConfig kisConfig;
	private final KisTokenService kisTokenService;
	private final ObjectMapper objectMapper;
	private final WebClient webClient;

	public KisStockService(KisConfig kisConfig, KisTokenService kisTokenService, ObjectMapper objectMapper) {

		this.kisConfig = kisConfig;
		this.kisTokenService = kisTokenService;
		this.objectMapper = objectMapper;

		this.webClient = WebClient.builder().baseUrl(kisConfig.getBaseUrl()).build();
	}

	// 현재가 조회
	public CurrentPriceResponse getCurrentPrice(String stockCode) {
		Map<String, Object> response = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/uapi/domestic-stock/v1/quotations/inquire-price")
						.queryParam("fid_cond_mrkt_div_code", "J").queryParam("fid_input_iscd", stockCode).build())
				.header("authorization", "Bearer " + kisTokenService.getAccessToken())
				.header("appkey", kisConfig.getAppkey()).header("appsecret", kisConfig.getAppsecret())
				.header("tr_id", "FHKST01010100").header("custtype", "P").retrieve()
				.onStatus(HttpStatusCode::isError,
						res -> res.bodyToMono(String.class).map(msg -> new RuntimeException("KIS HTTP 오류: " + msg)))
				.bodyToMono(Map.class).block();
		if (!"0".equals(response.get("rt_cd"))) {
			throw new RuntimeException("KIS 현재가 조회 오류: " + response.get("msg1"));
		}
		Map<String, Object> output = (Map<String, Object>) response.get("output");
		return objectMapper.convertValue(output, CurrentPriceResponse.class);
	}

	// 호가 조회
	public AskingPriceResponse getAskingPrice(String stockCode) {

		Map<String, Object> response = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn")
						.queryParam("fid_cond_mrkt_div_code", "J").queryParam("fid_input_iscd", stockCode).build())
				.header("authorization", "Bearer " + kisTokenService.getAccessToken())
				.header("appkey", kisConfig.getAppkey()).header("appsecret", kisConfig.getAppsecret())
				.header("tr_id", "FHKST01010200").header("custtype", "P").retrieve()
				.onStatus(HttpStatusCode::isError,
						res -> res.bodyToMono(String.class).map(msg -> new RuntimeException("KIS HTTP 오류: " + msg)))
				.bodyToMono(Map.class).block();

		if (!"0".equals(response.get("rt_cd"))) {
			throw new RuntimeException("KIS 호가 조회 오류: " + response.get("msg1"));
		}

		Map<String, Object> output = (Map<String, Object>) response.get("output1");

		return objectMapper.convertValue(output, AskingPriceResponse.class);
	}

}