package com.modsen.bookStorageService.controller;

import com.modsen.bookStorageService.dto.BookDto;
import com.modsen.bookStorageService.dto.UserDto;
import com.modsen.bookStorageService.service.BookService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto bookDTO) {
        BookDto createdBook = bookService.create(bookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping
    public ResponseEntity<Page<BookDto>> readAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDto> books = bookService.readAll(PageRequest.of(page, size));
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto> update(@PathVariable Long id,
                                       @RequestBody BookDto dto) {
        return new ResponseEntity<>(bookService.update(dto, id), HttpStatus.OK);
    }

    @PostMapping("/{id}/take")
    public HttpStatus takeBook(@PathVariable Long id,
                               @RequestBody UserDto dto) {
        bookService.takeBook(id, dto.username());
        return HttpStatus.OK;
    }

    @PostMapping("/{id}/return")
    public HttpStatus returnBook(@PathVariable Long id,
                                 @RequestBody UserDto dto) {
        bookService.returnBook(id, dto.username());
        return HttpStatus.OK;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return bookService.delete(id);
    }

    @GetMapping("/id/{id}")
    public BookDto getBooksByIds(@PathVariable Long id) {
        return bookService.getBooksByIds(id);
    }

    @GetMapping("/isbn/{isbn}")
    public BookDto getUserByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }
}
