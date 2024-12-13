package com.papa.app.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class KafkaService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Map<String, List<String>> messageHistory = new ConcurrentHashMap<>();

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String message) {
        messageHistory.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(message);
        kafkaTemplate.send(topic, message);
    }

    public CompletableFuture<List<String>> getHistory(String topic) {
        return CompletableFuture.completedFuture(
                messageHistory.getOrDefault(topic, new ArrayList<>()));
    }

    @KafkaListener(topicPattern = ".*")
    public void onMessage(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        messageHistory.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(message);
    }
}