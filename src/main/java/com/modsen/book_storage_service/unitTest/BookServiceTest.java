package com.modsen.book_storage_service.unitTest;
import com.modsen.book_storage_service.dto.BookDTO;
import com.modsen.book_storage_service.models.Book;
import com.modsen.book_storage_service.repository.BookRepository;
import com.modsen.book_storage_service.service.BookService;
import com.modsen.book_storage_service.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
        // Arrange
        BookDTO dto = new BookDTO();
        dto.setIsbn("123456789");
        dto.setTitle("Test Title");
        dto.setGenre("Fiction");
        dto.setDescription("Test Description");
        dto.setAuthor("Test Author");

        Book book = Book.builder()
                .id(1L) // Предполагаем, что ID будет установлен
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .genre(dto.getGenre())
                .description(dto.getDescription())
                .author(dto.getAuthor())
                .build();

        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book createdBook = bookService.create(dto);

        // Assert
        assertNotNull(createdBook);
        assertEquals(book.getId(), createdBook.getId());
        verify(bookRepository).save(any(Book.class));
        verify(kafkaProducerService).sendBookStatusUpdate(book.getId().toString(), "create");
    }

    @Test
    void testReadAll() {
        // Arrange
        Book book1 = new Book();
        Book book2 = new Book();
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        // Act
        List<Book> books = bookService.readAll();

        // Assert
        assertEquals(2, books.size());
        verify(bookRepository).findAll();
    }

    @Test
    void testUpdate() {
        // Arrange
        Book book = new Book();
        book.setId(1L); // Убедитесь, что ID установлен
        when(bookRepository.save(book)).thenReturn(book);

        // Act
        Book updatedBook = bookService.update(book);

        // Assert
        assertNotNull(updatedBook);
        verify(bookRepository).save(book);
    }

    @Test
    void testDelete() {
        // Arrange
        Long bookId = 1L;

        // Act
        bookService.delete(bookId);

        // Assert
        verify(bookRepository).deleteById(bookId);
        verify(kafkaProducerService).sendBookStatusUpdate(bookId.toString(), "delete");
    }

    @Test
    void testGetBooksByIds() {
        // Arrange
        Book book1 = new Book();
        Book book2 = new Book();
        List<Book> books = Arrays.asList(book1, book2);
        when(bookRepository.findAllById(any())).thenReturn(books);

        // Act
        List<Book> result = bookService.getBooksByIds(Arrays.asList(1L, 2L));

        // Assert
        assertEquals(2, result.size());
        verify(bookRepository).findAllById(any());
    }

    @Test
    void testGetUserByIsbn() {
        // Arrange
        String isbn = "123456789";
        Book book = new Book();
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        // Act
        Optional<Book> result = bookService.getUserByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(book, result.get());
        verify(bookRepository).findByIsbn(isbn);
    }

    @Test
    void testGetUserByIsbn_NotFound() {
        // Arrange
        String isbn = "unknownisbn";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        // Act
        Optional<Book> result = bookService.getUserByIsbn(isbn);

        // Assert
        assertFalse(result.isPresent());
        verify(bookRepository).findByIsbn(isbn);
    }
}