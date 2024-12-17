package com.modsen.bookStorageService.service;

import com.modsen.bookStorageService.dto.BookDTO;
import com.modsen.bookStorageService.exceptions.BusinessException;
import com.modsen.bookStorageService.models.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import com.modsen.bookStorageService.utils.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

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


    public BookDTO create(BookDTO dto) {
        if (bookRepository.findByIsbn(dto.isbn()).isPresent()) {
            throw new BusinessException("A book with the same ISBN already exists: " + dto.isbn());
        }
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

        return convertToDTO(savedBook);
    }

    private BookDTO convertToDTO(Book book) {
        return new BookDTO(book.getIsbn(),
                book.getTitle(),
                book.getGenre(),
                book.getDescription(),
                book.getAuthor());
    }

    public Page<Book> readAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Book update(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> delete(Long id) {
        bookRepository.deleteById(id);
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "delete");
        return null;
    }

    public List<Book> getBooksByIds(Long id) {
        if (bookRepository.findById(id).isEmpty()) {
            throw new BusinessException("Book not found with id: " + id);
        }
        return bookRepository.findAllById(Collections.singleton(id));
    }

    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BusinessException("Book not found with ISBN: " + isbn));
    }

    public String getBookStatusFromExternalApi(String bookId) {
        String url = "http://localhost:8082/api/book-status/" + bookId;

        HttpHeaders headers = new HttpHeaders();
        String token = jwtUtil.generateToken("Herman");
        headers.set("Authorization", "Bearer " + token); // Устанавливаем Bearer токен
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении статуса книги: " + e.getMessage());
        }
    }

    public void takeBook(Long id) {
        if (getBookStatusFromExternalApi(id.toString()).equals("\"CHECKED_OUT\"")) {
            throw new BusinessException("The book is already taken or the book does not exist");
        }
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "take");
    }

    public void returnBook(Long id) {
        if (getBookStatusFromExternalApi(id.toString()).equals("\"AVAILABLE\"")) {
            throw new BusinessException("The book is not occupied by anyone");
        }
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "return");
    }
}
