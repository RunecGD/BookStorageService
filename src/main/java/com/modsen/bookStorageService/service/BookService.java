package com.modsen.bookStorageService.service;

import com.modsen.bookStorageService.dto.BookDto;
import com.modsen.bookStorageService.dto.ResponseDto;
import com.modsen.bookStorageService.exception.BookAlreadyTakenException;
import com.modsen.bookStorageService.exception.BookNotFoundException;
import com.modsen.bookStorageService.exception.BookNotTakenException;
import com.modsen.bookStorageService.exception.ISBNAlreadyExistsException;
import com.modsen.bookStorageService.mapper.BookMapper;
import com.modsen.bookStorageService.model.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import com.modsen.bookStorageService.utils.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BookService {
    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;
    private final KafkaProducerService kafkaProducerService;
    private final JwtUtil jwtUtil;

    public BookService(RestTemplate restTemplate, BookRepository bookRepository, KafkaProducerService kafkaProducerService, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.bookRepository = bookRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.jwtUtil = jwtUtil;
    }


    @Transactional
    public ResponseDto create(BookDto dto) {
        bookRepository.findByIsbn(dto.isbn())
                .ifPresent(book -> {
                    throw new ISBNAlreadyExistsException("A book with the same ISBN already exists: " + dto.isbn());
                });
        Book book = Book.builder()
                .isbn(dto.isbn())
                .title(dto.title())
                .genre(dto.genre())
                .description(dto.description())
                .author(dto.author())
                .build();
        Book savedBook = bookRepository.save(book);

        kafkaProducerService.sendBookStatusUpdate(savedBook
                .getId()
                .toString(), "create");

        return BookMapper.INSTANCE.toDto(savedBook);
    }

    public Page<ResponseDto> readAll(Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return BookMapper.INSTANCE.toDtoPage(bookPage);
    }

    @Transactional
    public ResponseDto update(BookDto dto, Long id) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        bookRepository.findByIsbn(dto.isbn())
                .filter(book -> !book.getId().equals(existingBook.getId()))
                .ifPresent(book -> {
                    throw new ISBNAlreadyExistsException("A book with the same ISBN already exists: " + dto.isbn());
                });

        existingBook.setTitle(dto.title());
        existingBook.setAuthor(dto.author());
        existingBook.setIsbn(dto.isbn());

        Book updatedBook = bookRepository.save(existingBook);
        return BookMapper.INSTANCE.toDto(updatedBook);
    }

    @Transactional
    public ResponseEntity<String> delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with ID: " + id);
        }
        bookRepository.deleteById(id);
        return ResponseEntity.ok("Book deleted successfully.");
    }

    public ResponseDto getBooksByIds(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return BookMapper.INSTANCE.toDto(book);
    }

    public ResponseDto getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
        return BookMapper.INSTANCE.toDto(book);
    }

    public String getBookStatusFromExternalApi(String bookId, String username) {
        String url = "http://localhost:8082/books/tracker/book-status/" + bookId;

        HttpHeaders headers = new HttpHeaders();
        String token = jwtUtil.generateToken(username);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error when receiving book" + e.getMessage());
        }
    }

    @Transactional
    public void takeBook(Long id, String username) {
        if (getBookStatusFromExternalApi(id.toString(), username).equals("\"CHECKED_OUT\"")) {
            throw new BookAlreadyTakenException("The book is already taken or the book does not exist");
        }
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "take");
    }

    @Transactional
    public void returnBook(Long id, String username) {
        if (getBookStatusFromExternalApi(id.toString(), username).equals("\"AVAILABLE\"")) {
            throw new BookNotTakenException("The book is not occupied by anyone");
        }
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "return");
    }
}
