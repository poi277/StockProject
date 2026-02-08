package Poi.Stock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// 클라이언트가 구독할 경로
		config.enableSimpleBroker("/topic");
		// 클라이언트가 메시지를 보낼 경로
		config.setApplicationDestinationPrefixes("/app");
	}
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// WebSocket 연결 엔드포인트
		registry.addEndpoint("/ws").setAllowedOriginPatterns("*") // 프론트엔드 주소 (개발용)
				.withSockJS(); // SockJS 폴백 옵션
	}
}
