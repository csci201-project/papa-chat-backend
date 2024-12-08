package com.papa.app.interceptor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.papa.app.service.TopicService;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final String EXPECTED_TOKEN = "token";
    private final TopicService topicService;

    public WebSocketAuthInterceptor(TopicService topicService) {
        this.topicService = topicService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String path = request.getURI().getPath();
        String topic = path.substring(path.lastIndexOf('/') + 1);

        if (!topicService.topicExists(topic)) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return false;
        }

        String token = request.getURI().getQuery();
        if (token == null || !token.startsWith("token=")) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        token = token.substring(6);
        if (!EXPECTED_TOKEN.equals(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("topic", topic);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
    }
}