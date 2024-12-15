package com.papa.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
        try {
            System.out.println("Attempting to create topic: " + topic);
            boolean created = topicService.createTopic(topic);
            if (created) {
                System.out.println("Topic created successfully: " + topic);
                return ResponseEntity.ok("Topic created: " + topic);
            }
            return ResponseEntity.badRequest().body("Topic already exists: " + topic);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating topic: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    @DeleteMapping("/{topic}")
    public ResponseEntity<?> deleteTopic(@PathVariable String topic) {
        boolean deleted = topicService.deleteTopic(topic);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{topic}/exists")
    public ResponseEntity<?> checkTopicExists(@PathVariable String topic) {
        return ResponseEntity.ok(topicService.topicExists(topic));
    }
}