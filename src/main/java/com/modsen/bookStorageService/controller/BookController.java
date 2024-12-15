package com.modsen.bookStorageService.controller;

import com.modsen.bookStorageService.dto.BookDTO;
import com.modsen.bookStorageService.models.Book;
import com.modsen.bookStorageService.service.BookService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BookController {
    private final BookService bookService;


    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO bookDTO) {
        BookDTO createdBook = bookService.create(bookDTO);
        return ResponseEntity.ok(createdBook);
    }

    @GetMapping
    public ResponseEntity<Page<Book>> readAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Book> books = bookService.readAll(PageRequest.of(page, size));
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PutMapping
    @Transactional
    public ResponseEntity<Book> update(@RequestBody Book book) {
        return new ResponseEntity<>(bookService.update(book), HttpStatus.OK);
    }

    @PutMapping("/take/{id}")
    @Transactional
    public HttpStatus takeBook(@PathVariable Long id) {
        bookService.takeBook(id);
        return HttpStatus.OK;
    }

    @PutMapping("/return/{id}")
    @Transactional
    public HttpStatus returnBook(@PathVariable Long id) {
        bookService.returnBook(id);
        return HttpStatus.OK;
    }

    @DeleteMapping("/{id}")
    @Transactional
    public List<Book> delete(@PathVariable Long id) {
        return bookService.delete(id);
    }

    @GetMapping("/id/{id}")
    public List<Book> getBooksByIds(@PathVariable Long id) {
        return bookService.getBooksByIds(id);
    }

    @GetMapping("/isbn/{isbn}")
    public Book getUserByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }
}
