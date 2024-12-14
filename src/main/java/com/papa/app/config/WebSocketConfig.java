package com.papa.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.papa.app.handler.WebSocketHandler;
import com.papa.app.interceptor.WebSocketAuthInterceptor;
import com.papa.app.service.KafkaService;
import com.papa.app.service.TopicService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TopicService topicService;
    private final KafkaService kafkaService;

    public WebSocketConfig(TopicService topicService, KafkaService kafkaService) {
        this.topicService = topicService;
        this.kafkaService = kafkaService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(kafkaService), "/ws/chat/{topic}")
                .addInterceptors(new WebSocketAuthInterceptor(topicService))
                .setAllowedOrigins("*");
    }
}