package com.papa.app.handler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;

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
        System.out.println("New WebSocket connection established for topic: " + topic);
        topicSessions.computeIfAbsent(topic, k -> new ConcurrentHashMap<>())
                .put(session, topic);
        System.out.println("Total sessions for topic " + topic + ": " + topicSessions.get(topic).size());

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
    
    /**
     * Censors bad words in the given text by replacing them with asterisks.
     * 
     * @param text The original text.
     * @return The censored text.
     */
    private String censorBadWords(String text) {
        if (text == null) {
            return null; // Handle null messages gracefully
        }

        // List of inappropriate/bad words to censor
        List<String> badWords = Arrays.asList("fuck", "shit", "bitch", "UCLA", "FUCK", "SHIT", "BITCH", "ucla", "FUCKING", "fucking");

        for (String badWord : badWords) {
            // Replace bad word with asterisks
            String replacement = "*".repeat(badWord.length());
            text = text.replaceAll("(?i)\\b" + badWord + "\\b", replacement);
        }

        return text;
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    	//System.out.println(message.getPayload().toString());
        String payload = message.getPayload().toString();
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        chatMessage.setMessage(censorBadWords(chatMessage.getMessage()));
        String topic = extractTopicFromSession(session);
        String jsonMessage = objectMapper.writeValueAsString(chatMessage);

        // Broadcast immediately to all clients
        Map<WebSocketSession, String> sessions = topicSessions.get(topic);
        if (sessions != null) {
            TextMessage textMessage = new TextMessage(jsonMessage);
            for (WebSocketSession clientSession : sessions.keySet()) {
                if (clientSession.isOpen()) {
                    clientSession.sendMessage(textMessage);
                }
            }
        }

        // Send to Kafka asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                kafkaService.send(topic, jsonMessage);
            } catch (Exception e) {
                System.err.println("Failed to send to Kafka: " + e.getMessage());
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String topic = extractTopicFromSession(session);
        System.out.println("Connection closed for session: " + session.getId() + " in topic: " + topic);
        Map<WebSocketSession, String> sessions = topicSessions.get(topic);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                topicSessions.remove(topic);
            }
            System.out.println("Remaining sessions in topic " + topic + ": " +
                    (sessions.isEmpty() ? 0 : sessions.size()));
        }
    }

    private String extractTopicFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}