package com.modsen.bookStorageService.controller;

import com.modsen.bookStorageService.dto.BookDto;
import com.modsen.bookStorageService.dto.ResponseDto;
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
    public ResponseEntity<ResponseDto> createBook(@RequestBody BookDto dto) {
        ResponseDto createdBook = bookService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping
    public ResponseEntity<Page<ResponseDto>> readAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ResponseDto> books = bookService.readAll(PageRequest.of(page, size));
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> update(@PathVariable Long id,
                                              @RequestBody BookDto dto) {
        ResponseDto updatedBook = bookService.update(dto, id);
        return ResponseEntity.ok(updatedBook);
    }

    @PostMapping("/{id}/take")
    public ResponseEntity<String> takeBook(@PathVariable Long id,
                                           @RequestBody UserDto dto) {
        bookService.takeBook(id, dto.username());
        return ResponseEntity.ok("Book taken successfully.");
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<String> returnBook(@PathVariable Long id,
                                             @RequestBody UserDto dto) {
        bookService.returnBook(id, dto.username());
        return ResponseEntity.ok("Book returned successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return bookService.delete(id);
    }

    @GetMapping("/id/{id}")
    public ResponseDto getBooksByIds(@PathVariable Long id) {
        return bookService.getBooksByIds(id);
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseDto getUserByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }
}
