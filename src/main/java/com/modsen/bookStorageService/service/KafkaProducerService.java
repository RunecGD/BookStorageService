package com.modsen.bookStorageService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendBookStatusUpdate(String bookId, String action) {
        String message = String.format("{\"action\":\"%s\", \"bookId\":\"%s\"}",
                action,
                bookId
        );
        kafkaTemplate.send("book-status-topic", message);
    }
}