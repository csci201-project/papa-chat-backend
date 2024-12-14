package com.papa.app.handler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.papa.app.dto.ChatMessage;
import com.papa.app.service.KafkaService;

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
                    ChatMessage chatMessage = objectMapper.readValue(msg, ChatMessage.class);
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
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
            String jsonMessage = objectMapper.writeValueAsString(chatMessage);

            // INSERT CHAT FILTERING HERE

            // Broadcast to other clients
            Map<WebSocketSession, String> sessions = topicSessions.get(topic);
            if (sessions != null) {
                sessions.forEach((clientSession, t) -> {
                    if (clientSession.isOpen()) {
                        try {
                            clientSession.sendMessage(new TextMessage(jsonMessage));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            // Send to Kafka asynchronously
            CompletableFuture.runAsync(() -> {
                kafkaService.send(topic, jsonMessage);
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
}