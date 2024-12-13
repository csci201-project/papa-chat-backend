package com.papa.app.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.papa.app.dto.ChatMessage;
import com.papa.app.service.KafkaService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class WebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<WebSocketSession, String>> topicSessions = new ConcurrentHashMap<>();
    private final KafkaService kafkaService;

    public WebSocketHandler(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String topic = extractTopicFromSession(session);
        topicSessions.computeIfAbsent(topic, k -> new ConcurrentHashMap<>())
                .put(session, topic);

        // Load history from Kafka when client connects
        kafkaService.getHistory(topic).thenAccept(messages -> {
            messages.forEach(msg -> {
                try {
                    session.sendMessage(new TextMessage(formatMessage(msg)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String topic = extractTopicFromSession(session);
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            String formattedMessage = formatMessage(chatMessage.getMessage());

            // 1. First broadcast instantly via WebSocket
            Map<WebSocketSession, String> sessions = topicSessions.get(topic);
            if (sessions != null) {
                sessions.forEach((clientSession, t) -> {
                    if (clientSession != session && clientSession.isOpen()) {
                        try {
                            clientSession.sendMessage(new TextMessage(formattedMessage));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            // 2. Then send to Kafka asynchronously for persistence
            CompletableFuture.runAsync(() -> {
                kafkaService.send(topic, chatMessage.getMessage());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String topic = extractTopicFromSession(session);
        Map<WebSocketSession, String> sessions = topicSessions.get(topic);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                topicSessions.remove(topic);
            }
        }
    }

    private String extractTopicFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String formatMessage(String message) {
        return String.format("{\"type\":\"chat\",\"message\":\"%s\"}", message);
    }
}