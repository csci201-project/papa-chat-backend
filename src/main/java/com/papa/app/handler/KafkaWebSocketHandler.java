package com.papa.app.handler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.papa.app.dto.BaseMessage;
import com.papa.app.dto.ChatMessage;
import com.papa.app.service.KafkaService;
import com.papa.app.service.TopicService;

public class KafkaWebSocketHandler extends TextWebSocketHandler {
    private final KafkaService kafkaService;
    private final TopicService topicService;
    private final ConcurrentHashMap<String, Consumer<String>> sessionListeners = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaWebSocketHandler(KafkaService kafkaService, TopicService topicService) {
        this.kafkaService = kafkaService;
        this.topicService = topicService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String topic = (String) session.getAttributes().get("topic");
        Consumer<String> messageListener = message -> {
            try {
                ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        sessionListeners.put(session.getId(), messageListener);
        kafkaService.subscribe(topic, messageListener);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String topic = (String) session.getAttributes().get("topic");
        try {
            BaseMessage baseMessage = objectMapper.readValue(message.getPayload(), BaseMessage.class);

            if (baseMessage.getType() == null) {
                session.sendMessage(new TextMessage("{\"error\": \"Message type is required\"}"));
                return;
            }

            switch (baseMessage.getType()) {
                case "chat":
                    ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
                    if (chatMessage.getMessage() == null) {
                        session.sendMessage(new TextMessage("{\"error\": \"Message is required\"}"));
                        return;
                    }
                    kafkaService.send(topic, message.getPayload());
                    break;
                default:
                    session.sendMessage(new TextMessage("{\"error\": \"Unsupported message type\"}"));
                    break;
            }
        } catch (Exception e) {
            try {
                session.sendMessage(new TextMessage("{\"error\": \"Invalid JSON format\"" + e +  "}"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String topic = (String) session.getAttributes().get("topic");
        Consumer<String> listener = sessionListeners.remove(session.getId());
        if (listener != null) {
            kafkaService.unsubscribe(topic, listener);
        }
    }
}