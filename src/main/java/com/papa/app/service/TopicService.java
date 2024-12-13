package com.papa.app.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TopicService {
    private final Set<String> topics = ConcurrentHashMap.newKeySet();

    public boolean createTopic(String topic) {
        return topics.add(topic);
    }

    public boolean topicExists(String topic) {
        return topics.contains(topic);
    }

    public Set<String> getAllTopics() {
        return topics;
    }

    public boolean deleteTopic(String topic) {
        return topics.remove(topic);
    }
}