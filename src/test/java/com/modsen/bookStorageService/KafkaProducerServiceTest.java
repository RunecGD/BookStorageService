package com.modsen.bookStorageService;

import com.modsen.bookStorageService.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendBookStatusUpdate() {
        // Arrange
        String bookId = "1";
        String action = "create";

        // Act
        kafkaProducerService.sendBookStatusUpdate(bookId, action);

        // Assert
        String expectedMessage = "{\"action\":\"create\", \"bookId\":\"1\"}";
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), messageCaptor.capture());

        assertEquals("book-status-topic", topicCaptor.getValue());
        assertEquals(expectedMessage, messageCaptor.getValue());
    }
}
