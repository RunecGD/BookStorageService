package com.modsen.bookStorageService;
import com.modsen.bookStorageService.dto.BookDTO;
import com.modsen.bookStorageService.exceptions.BusinessException;
import com.modsen.bookStorageService.models.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import com.modsen.bookStorageService.service.KafkaProducerService;
import com.modsen.bookStorageService.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @Mock
    private RestTemplate restTemplate;

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
    public void testCreateBookSuccess() {
        BookDTO dto = new BookDTO("1234567890", "Book Title", "Fiction", "Description", "Author");
        Book book = new Book();
        book.setIsbn(dto.isbn());
        book.setTitle(dto.title());
        book.setGenre(dto.genre());
        book.setDescription(dto.description());
        book.setAuthor(dto.author());

        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDTO result = bookService.create(dto);

        assertNotNull(result);
        assertEquals(dto.isbn(), result.isbn());
        verify(kafkaProducerService).sendBookStatusUpdate(anyString(), eq("create"));
    }

    @Test
    public void testCreateBookWithExistingIsbn() {
        BookDTO dto = new BookDTO("1234567890", "Book Title", "Fiction", "Description", "Author");
        Book existingBook = new Book();
        existingBook.setIsbn(dto.isbn());

        when(bookRepository.findByIsbn(dto.isbn())).thenReturn(Optional.of(existingBook));

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.create(dto));
        assertEquals("A book with the same ISBN already exists: 1234567890", exception.getMessage());
    }

    @Test
    public void testGetBookByIsbnFound() {
        String isbn = "1234567890";
        Book book = new Book();
        book.setIsbn(isbn);

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        Book result = bookService.getBookByIsbn(isbn);
        assertNotNull(result);
        assertEquals(isbn, result.getIsbn());
    }

    @Test
    public void testGetBookByIsbnNotFound() {
        String isbn = "1234567890";

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.getBookByIsbn(isbn));
        assertEquals("Book not found with ISBN: 1234567890", exception.getMessage());
    }

    @Test
    public void testTakeBookAlreadyCheckedOut() {
        String bookId = "1";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("CHECKED_OUT");

        BusinessException exception = assertThrows(BusinessException.class, () -> bookService.takeBook(Long.valueOf(bookId)));
        assertEquals("The book is already taken or the book does not exist", exception.getMessage());
    }

    @Test
    public void testReturnBook() {
        String bookId = "1";
        bookService.returnBook(Long.valueOf(bookId));
        verify(kafkaProducerService).sendBookStatusUpdate(bookId, "return");
    }
}