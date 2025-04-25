package com.example.hrms.config;

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
    // Định nghĩa các destination prefix cho các tin nhắn được gửi đến client
    config.enableSimpleBroker("/topic", "/queue");

    // Các tin nhắn gửi đến server sẽ có prefix /app
    config.setApplicationDestinationPrefixes("/app");

    // Cấu hình đường dẫn user để gửi tin nhắn cá nhân
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Đăng ký endpoint cho WebSocket với hỗ trợ fallback SockJS
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }
}