package com.modsen.book_storage_service.controller;

import com.modsen.book_storage_service.dto.BookDTO;
import com.modsen.book_storage_service.models.Book;
import com.modsen.book_storage_service.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Book> create(@RequestBody BookDTO dto) {
        return new ResponseEntity<>(bookService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Book>> readAll() {
        return new ResponseEntity<>(bookService.readAll(), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Book> update(@RequestBody Book book) {
        return new ResponseEntity<>(bookService.update(book), HttpStatus.OK);

    }


    @DeleteMapping("/{id}")
    public HttpStatus delete(@PathVariable Long id) {
        bookService.delete(id);
        return HttpStatus.OK;
    }

    @GetMapping("/id/{id}")
    public List<Book> getBooksByIds(@PathVariable Long id) {
        return bookService.getBooksByIds(Collections.singleton(id));
    }

    @GetMapping("/isbn/{isbn}")
    public Optional<Book> getUserByIsbn(@PathVariable String isbn) {
        return bookService.getUserByIsbn(isbn); // Обработка GET-запроса
    }
}
