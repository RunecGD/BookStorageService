package com.modsen.book_storage_service.service;

import com.modsen.book_storage_service.dto.BookDTO;
import com.modsen.book_storage_service.models.Book;
import com.modsen.book_storage_service.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public Book create(BookDTO dto) {
        Book book = Book.builder().isbn(dto.getIsbn()).
                title(dto.getTitle()).genre(dto.getGenre())
                .description(dto.getDescription()).author(dto.getAuthor()).build();
        Book savedBook=bookRepository.save(book);
        kafkaProducerService.sendBookStatusUpdate(savedBook.getId().toString(), "create");
        return book;
    }

    public List<Book> readAll() {
        return bookRepository.findAll();
    }

    public Book update(Book book) {
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        bookRepository.deleteById(id);
        kafkaProducerService.sendBookStatusUpdate(id.toString(), "delete");

    }

    public List<Book> getBooksByIds(Iterable<Long> id) {
        return (List<Book>) bookRepository.findAllById(id);
    }

    public Optional<Book> getUserByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn); // Использование метода поиска
    }
}
