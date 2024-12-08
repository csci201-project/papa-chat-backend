package com.papa.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.papa.app.handler.KafkaWebSocketHandler;
import com.papa.app.interceptor.WebSocketAuthInterceptor;
import com.papa.app.service.KafkaService;
import com.papa.app.service.TopicService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final KafkaService kafkaService;
    private final TopicService topicService;

    public WebSocketConfig(KafkaService kafkaService, TopicService topicService) {
        this.kafkaService = kafkaService;
        this.topicService = topicService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new KafkaWebSocketHandler(kafkaService, topicService), "/ws/chat/{topic}")
               .addInterceptors(new WebSocketAuthInterceptor(topicService))
               .setAllowedOrigins("*");
    }
}