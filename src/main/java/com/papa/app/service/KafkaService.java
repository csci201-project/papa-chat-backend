package com.papa.app.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.kafka.core.*;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class KafkaService {
    private final Map<String, CopyOnWriteArrayList<Consumer<String>>> topicListeners = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaListenerEndpointRegistry registry;
    private final KafkaAdmin kafkaAdmin;

    @Autowired
    public KafkaService(KafkaTemplate<String, String> kafkaTemplate, 
                       KafkaListenerEndpointRegistry registry,
                       KafkaAdmin kafkaAdmin) {
        this.kafkaTemplate = kafkaTemplate;
        this.registry = registry;
        this.kafkaAdmin = kafkaAdmin;
    }

    public void subscribe(String topic, Consumer<String> listener) {
        topicListeners.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(listener);
        
        // Create topic if it doesn't exist
        NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
        kafkaAdmin.createOrModifyTopics(newTopic);
    }

    public void unsubscribe(String topic, Consumer<String> listener) {
        CopyOnWriteArrayList<Consumer<String>> listeners = topicListeners.get(topic);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                topicListeners.remove(topic);
            }
        }
    }

    @KafkaListener(id = "dynamicListener", topicPattern = ".*")
    public void onMessage(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        CopyOnWriteArrayList<Consumer<String>> listeners = topicListeners.get(topic);
        if (listeners != null) {
            listeners.forEach(listener -> {
                try {
                    listener.accept(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}