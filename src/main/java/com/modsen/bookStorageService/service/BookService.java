package com.modsen.bookStorageService.service;

import com.modsen.bookStorageService.dto.BookRequestDto;
import com.modsen.bookStorageService.dto.BookResponseDto;
import com.modsen.bookStorageService.exception.BookAlreadyTakenException;
import com.modsen.bookStorageService.exception.BookNotFoundException;
import com.modsen.bookStorageService.exception.BookNotTakenException;
import com.modsen.bookStorageService.exception.ISBNAlreadyExistsException;
import com.modsen.bookStorageService.mapper.BookMapper;
import com.modsen.bookStorageService.model.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import com.modsen.bookStorageService.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BookService {
    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;
    private final KafkaProducerService kafkaProducerService;
    private final JwtUtil jwtUtil;

    @Transactional
    public BookResponseDto create(BookRequestDto dto) {
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
        bookRepository.save(book);

        kafkaProducerService.sendBookStatusUpdate(book
                .getId()
                .toString(), "create");

        return BookMapper.INSTANCE.toDto(book);
    }

    public Page<BookResponseDto> readAll(Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return BookMapper.INSTANCE.toDtoPage(bookPage);
    }

    @Transactional
    public BookResponseDto update(BookRequestDto dto, Long id) {
        Book bookForUpdate = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        bookRepository.findByIsbn(dto.isbn())
                .filter(book -> !book.getId().equals(bookForUpdate.getId()))
                .ifPresent(book -> {
                    throw new ISBNAlreadyExistsException("A book with the same ISBN already exists: " + dto.isbn());
                });

        bookForUpdate.setTitle(dto.title());
        bookForUpdate.setAuthor(dto.author());
        bookForUpdate.setIsbn(dto.isbn());

        bookRepository.save(bookForUpdate);
        return BookMapper.INSTANCE.toDto(bookForUpdate);
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with ID: " + id);
        }
        bookRepository.deleteById(id);
        ResponseEntity.noContent().build();
    }

    public BookResponseDto getBooksByIds(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return BookMapper.INSTANCE.toDto(book);
    }

    public BookResponseDto getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
        return BookMapper.INSTANCE.toDto(book);
    }

    public String getBookStatusFromExternalApi(Long bookId, String username) {
        String url = "http://localhost:8082/books/tracker/book-status/" + bookId;

        HttpHeaders headers = new HttpHeaders();
        String token = jwtUtil.generateToken(username);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            throw new BookNotFoundException("Error when receiving book" + e.getMessage());
        }
    }

    @Transactional
    public void takeBook(Long id, String username) {
        if (getBookStatusFromExternalApi(id, username).equals("\"CHECKED_OUT\"")) {
            throw new BookAlreadyTakenException("The book is already taken or the book does not exist");
        }
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "take");
    }

    @Transactional
    public void returnBook(Long id, String username) {
        if (getBookStatusFromExternalApi(id, username).equals("\"AVAILABLE\"")) {
            throw new BookNotTakenException("The book is not occupied by anyone");
        }
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "return");
    }
}
