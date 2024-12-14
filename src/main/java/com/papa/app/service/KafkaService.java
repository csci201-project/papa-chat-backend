package com.papa.app.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Properties consumerProperties;

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.consumerProperties = new Properties();
        this.consumerProperties.put("bootstrap.servers", "localhost:9092");
        this.consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumerProperties.put("group.id", "history-reader-group");
        this.consumerProperties.put("auto.offset.reset", "earliest");
    }

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    public CompletableFuture<List<String>> getHistory(String topic) {
        return CompletableFuture.supplyAsync(() -> {
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties)) {
                List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                        .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                        .toList();
                
                consumer.assign(partitions);
                consumer.seekToBeginning(partitions);
                
                List<String> messages = new ArrayList<>();
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    messages.add(record.value());
                }
                
                return messages;
            }
        });
    }

    @KafkaListener(topicPattern = ".*")
    public void onMessage(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        // Just log or process the message as needed
    }
}