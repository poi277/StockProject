package Poi.Stock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
		scheduler.initialize();

		config.enableSimpleBroker("/topic", "/queue") // ✅ /queue 추가
              .setHeartbeatValue(new long[]{10000, 10000})
				.setTaskScheduler(scheduler);
        config.setApplicationDestinationPrefixes("/app");
		config.setUserDestinationPrefix("/user"); // ✅ 유저별 전송용
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

	// 연결 시 헤더에서 userId 꺼내서 Principal로 등록
	// WebSocketConfig.java
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

				if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
					String userId = accessor.getFirstNativeHeader("userId");
					if (userId != null) {
						accessor.setUser(new StompPrincipal(userId));
					}
				}
				return message;
			}
		});
	}
}
