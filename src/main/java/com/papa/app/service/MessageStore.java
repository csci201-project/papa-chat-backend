package com.papa.app.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageStore {
    private final Map<String, List<String>> topicMessages = new ConcurrentHashMap<>();

    public void addMessage(String topic, String message) {
        topicMessages.computeIfAbsent(topic, k -> new ArrayList<>()).add(message);
    }

    public List<String> getMessages(String topic) {
        return topicMessages.getOrDefault(topic, new ArrayList<>());
    }
}