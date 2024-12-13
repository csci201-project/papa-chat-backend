package com.papa.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.papa.app.service.KafkaService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final KafkaService kafkaService;

    public MessageController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    // Send a message to a topic
    @PostMapping("/{topic}")
    public ResponseEntity<?> sendMessage(
            @PathVariable String topic,
            @RequestBody String message) {
        kafkaService.send(topic, message);
        return ResponseEntity.ok().build();
    }
}