package Poi.Stock.config.kis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "kis")
@Getter
@Setter
public class KisConfig {

	private String appkey;
	private String appsecret;
	private String baseUrl;

}