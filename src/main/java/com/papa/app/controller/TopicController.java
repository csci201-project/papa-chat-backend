package com.papa.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.papa.app.service.TopicService;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/{topic}")
    public ResponseEntity<String> createTopic(@PathVariable String topic) {
        boolean created = topicService.createTopic(topic);
        if (created) {
            return ResponseEntity.ok("Topic created: " + topic);
        }
        return ResponseEntity.badRequest().body("Topic already exists: " + topic);
    }

    @GetMapping
    public ResponseEntity<?> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }
}