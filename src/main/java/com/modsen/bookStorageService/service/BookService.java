package com.modsen.bookStorageService.service;

import com.modsen.bookStorageService.dto.BookDTO;
import com.modsen.bookStorageService.models.Book;
import com.modsen.bookStorageService.repository.BookRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public List<Book> getBooksByIds(Iterable<Long> id) {
        return bookRepository.findAllById(id);
    }

    public Book getUserByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));    }

    public void takeBook(Long id) {
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "take");
    }

    public void returnBook(Long id) {
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "return");
    }
}
