package com.papa.app.service;

import com.papa.app.dto.ChatMessage;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageService {
    private final Map<String, List<ChatMessage>> messageHistory = new ConcurrentHashMap<>();

    public void saveMessage(String topic, ChatMessage message) {
        messageHistory.computeIfAbsent(topic, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(message);
    }

    public List<ChatMessage> getMessageHistory(String topic) {
        return messageHistory.getOrDefault(topic, Collections.emptyList());
    }

    public void clearTopic(String topic) {
        messageHistory.remove(topic);
    }
}