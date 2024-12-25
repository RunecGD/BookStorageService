package com.modsen.bookStorageService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class KafkaProducerServiceTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    public void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
    }

    @Test
    public void testSendBookStatusUpdate() {
        String bookId = "1";
        String action = "create";
        String expectedMessage = "{\"action\":\"create\", \"bookId\":\"1\"}";

        kafkaProducerService.sendBookStatusUpdate(bookId, action);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), messageCaptor.capture());

        assertEquals("book-status-topic", topicCaptor.getValue());
        assertEquals(expectedMessage, messageCaptor.getValue());
    }
}
