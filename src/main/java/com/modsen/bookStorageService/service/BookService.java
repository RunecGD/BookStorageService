package com.modsen.bookStorageService.service;

import com.modsen.bookStorageService.dto.BookDTO;
import com.modsen.bookStorageService.exceptions.BusinessException;
import com.modsen.bookStorageService.models.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final KafkaProducerService kafkaProducerService;

    public BookService(BookRepository bookRepository, KafkaProducerService kafkaProducerService) {
        this.bookRepository = bookRepository;
        this.kafkaProducerService = kafkaProducerService;
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


    public void takeBook(Long id) {
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "take");
    }

    public void returnBook(Long id) {
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "return");
    }
}
