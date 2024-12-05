package com.modsen.book_storage_service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookStatusUpdate(String bookId, String action) {
        String message = String.format("{\"action\":\"%s\", \"bookId\":\"%s\"}", action, bookId);
        kafkaTemplate.send("book-status-topic", message);
    }
}