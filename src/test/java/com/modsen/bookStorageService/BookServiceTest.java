package com.modsen.bookStorageService;

import com.modsen.bookStorageService.dto.BookRequestDto;
import com.modsen.bookStorageService.dto.BookResponseDto;
import com.modsen.bookStorageService.exception.BookNotFoundException;
import com.modsen.bookStorageService.exception.ISBNAlreadyExistsException;
import com.modsen.bookStorageService.model.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import com.modsen.bookStorageService.service.BookService;
import com.modsen.bookStorageService.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateBook_Success() {
        BookRequestDto dto = new BookRequestDto(1L, "978-1-56619-909-4", "Brave New World", "Dystopian", "A novel", "Aldous Huxley");
        Book book = new Book(1L, dto.isbn(), dto.title(), dto.genre(), dto.description(), dto.author());

        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponseDto response = bookService.create(dto);

        assertNotNull(response);
        assertEquals(dto.isbn(), response.isbn());
        verify(kafkaProducerService).sendBookStatusUpdate("1", "create");
    }

    @Test
    public void testCreateBook_ISBNAlreadyExists() {
        BookRequestDto dto = new BookRequestDto(1L, "978-1-56619-909-4", "Brave New World", "Dystopian", "A novel", "Aldous Huxley");
        Book existingBook = new Book(1L, dto.isbn(), "Another Title", "Dystopian", "Another description", "Another Author");

        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.of(existingBook));

        assertThrows(ISBNAlreadyExistsException.class, () -> bookService.create(dto));
    }

    @Test
    public void testReadAllBooks() {
        Pageable pageable = Pageable.unpaged();
        Book book = new Book(1L, "978-1-56619-909-4", "Brave New World", "Dystopian", "A novel", "Aldous Huxley");
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookResponseDto> response = bookService.readAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    public void testUpdateBook_Success() {
        Long bookId = 1L;
        BookRequestDto dto = new BookRequestDto(1L, "978-1-56619-909-4", "Updated Title", "Dystopian", "Updated description", "Aldous Huxley");
        Book existingBook = new Book(bookId, "978-1-56619-909-4", "Old Title", "Dystopian", "Old description", "Aldous Huxley");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        BookResponseDto response = bookService.update(dto, bookId);

        assertEquals(dto.title(), response.title());
    }

    @Test
    public void testDeleteBook_Success() {
        Long bookId = 1L;

        when(bookRepository.existsById(bookId)).thenReturn(true);

        assertDoesNotThrow(() -> bookService.delete(bookId));
        verify(bookRepository).deleteById(bookId);
    }

    @Test
    public void testDeleteBook_NotFound() {
        Long bookId = 1L;

        when(bookRepository.existsById(bookId)).thenReturn(false);

        assertThrows(BookNotFoundException.class, () -> bookService.delete(bookId));
    }

    @Test
    public void testGetBookByIdNotFound() {
        Long bookId = 1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBooksByIds(bookId));
    }

    @Test
    public void testGetBookByISBN() {
        String isbn = "978-1-56619-909-4";
        Book book = new Book(1L, isbn, "Brave New World", "Dystopian", "A novel", "Aldous Huxley");

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        BookResponseDto response = bookService.getBookByIsbn(isbn);

        assertNotNull(response);
        assertEquals(isbn, response.isbn());
    }
}
