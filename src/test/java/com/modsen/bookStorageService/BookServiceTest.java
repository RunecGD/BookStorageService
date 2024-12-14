package com.modsen.bookStorageService;

import com.modsen.bookStorageService.dto.BookDTO;
import com.modsen.bookStorageService.exceptions.BusinessException;
import com.modsen.bookStorageService.models.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import com.modsen.bookStorageService.service.BookService;
import com.modsen.bookStorageService.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    private BookRepository bookRepository;
    private KafkaProducerService kafkaProducerService;
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        bookRepository = mock(BookRepository.class);
        kafkaProducerService = mock(KafkaProducerService.class);
        bookService = new BookService(bookRepository, kafkaProducerService);
    }

    @Test
    public void testCreateBook_Success() {
        BookDTO bookDTO = new BookDTO("1234567890", "Test Title", "Fiction", "Test Description", "Test Author");
        Book savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setIsbn(bookDTO.isbn());
        savedBook.setTitle(bookDTO.title());
        savedBook.setGenre(bookDTO.genre());
        savedBook.setDescription(bookDTO.description());
        savedBook.setAuthor(bookDTO.author());

        when(bookRepository.findByIsbn(bookDTO.isbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookDTO result = bookService.create(bookDTO);

        assertEquals(bookDTO.isbn(), result.isbn());
        assertEquals(bookDTO.title(), result.title());
        verify(kafkaProducerService).sendBookStatusUpdate("1", "create");
    }

    @Test
    public void testCreateBook_DuplicateIsbn() {
        BookDTO bookDTO = new BookDTO("1234567890", "Test Title", "Fiction", "Test Description", "Test Author");
        when(bookRepository.findByIsbn(bookDTO.isbn())).thenReturn(Optional.of(new Book()));

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.create(bookDTO));
        assertEquals("A book with the same ISBN already exists: " + bookDTO.isbn(), exception.getMessage());
    }

    @Test
    public void testReadAllBooks() {
        Pageable pageable = mock(Pageable.class);
        Page<Book> bookPage = mock(Page.class);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<Book> result = bookService.readAll(pageable);

        assertEquals(bookPage, result);
    }

    @Test
    public void testUpdateBook() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.update(book);

        assertEquals(book, result);
    }

    @Test
    public void testDeleteBook() {
        Long bookId = 1L;

        bookService.delete(bookId);

        verify(bookRepository).deleteById(bookId);
        verify(kafkaProducerService).sendBookStatusUpdate(bookId.toString(), "delete");
    }

    @Test
    public void testGetBooksByIds_BookFound() {
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.findAllById(Collections.singleton(bookId))).thenReturn(Collections.singletonList(book));

        List<Book> result = bookService.getBooksByIds(bookId);

        assertEquals(Collections.singletonList(book), result);
    }

    @Test
    public void testGetBooksByIds_BookNotFound() {
        // Given
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.getBooksByIds(bookId));
        assertEquals("Book not found with id: " + bookId, exception.getMessage());
    }

    @Test
    public void testGetBookByIsbn_BookFound() {
        String isbn = "1234567890";
        Book book = new Book();
        book.setIsbn(isbn);
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        Book result = bookService.getBookByIsbn(isbn);

        assertEquals(book, result);
    }

    @Test
    public void testGetBookByIsbn_BookNotFound() {
        String isbn = "1234567890";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.getBookByIsbn(isbn));
        assertEquals("Book not found with ISBN: " + isbn, exception.getMessage());
    }

    @Test
    public void testTakeBook() {
        Long bookId = 1L;

        bookService.takeBook(bookId);

        verify(kafkaProducerService).sendBookStatusUpdate(bookId.toString(), "take");
    }

    @Test
    public void testReturnBook() {
        Long bookId = 1L;

        bookService.returnBook(bookId);

        verify(kafkaProducerService).sendBookStatusUpdate(bookId.toString(), "return");
    }
}