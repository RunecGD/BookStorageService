package com.modsen.bookStorageService;

import com.modsen.bookStorageService.dto.BookDTO;
import com.modsen.bookStorageService.exceptions.BusinessException;
import com.modsen.bookStorageService.models.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import com.modsen.bookStorageService.service.BookService;
import com.modsen.bookStorageService.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateBookSuccess() {
        BookDTO dto = new BookDTO("1234567890", "Test Title", "Fiction", "Test Description", "Test Author");
        Book book = new Book();
        book.setId(1L); // Устанавливаем id
        book.setIsbn(dto.isbn());
        book.setTitle(dto.title());
        book.setGenre(dto.genre());
        book.setDescription(dto.description());
        book.setAuthor(dto.author());

        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDTO createdBook = bookService.create(dto);

        assertEquals(dto.isbn(), createdBook.isbn());
        assertEquals(dto.title(), createdBook.title());
        verify(kafkaProducerService).sendBookStatusUpdate("1", "create"); // Используем строковое представление id
    }

    @Test
    public void testCreateBookAlreadyExists() {
        BookDTO dto = new BookDTO("1234567890", "Test Title", "Fiction", "Test Description", "Test Author");
        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.of(new Book()));

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.create(dto));
        assertEquals("A book with the same ISBN already exists: 1234567890", exception.getMessage());
    }

    @Test
    public void testGetBookByIsbnSuccess() {
        String isbn = "1234567890";
        Book book = new Book();
        book.setIsbn(isbn);

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        Book foundBook = bookService.getBookByIsbn(isbn);

        assertEquals(isbn, foundBook.getIsbn());
    }

    @Test
    public void testGetBookByIsbnNotFound() {
        String isbn = "1234567890";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.getBookByIsbn(isbn));
        assertEquals("Book not found with ISBN: 1234567890", exception.getMessage());
    }

    @Test
    public void testDeleteBook() {
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(new Book()));

        bookService.delete(bookId);

        verify(bookRepository).deleteById(bookId);
        verify(kafkaProducerService).sendBookStatusUpdate(anyString(), eq("delete"));
    }

    @Test
    public void testGetBooksByIdsNotFound() {
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.getBooksByIds(bookId));
        assertEquals("Book not found with id: 1", exception.getMessage());
    }

    // Добавьте дополнительные тесты для других методов, таких как takeBook, returnBook и т.д.
}