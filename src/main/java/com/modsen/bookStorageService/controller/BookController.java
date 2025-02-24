package com.modsen.bookStorageService.controller;

import com.modsen.bookStorageService.dto.BookRequestDto;
import com.modsen.bookStorageService.dto.BookResponseDto;
import com.modsen.bookStorageService.dto.UserDto;
import com.modsen.bookStorageService.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "http://0.0.0.0:8000/")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@RequestBody BookRequestDto dto) {
        BookResponseDto createdBook = bookService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdBook);
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDto>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookResponseDto> books = bookService.readAll(PageRequest.of(page, size));
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(@PathVariable Long id,
                                                      @RequestBody BookRequestDto dto) {
        BookResponseDto updatedBook = bookService.update(dto, id);
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
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public BookResponseDto getBookByIds(@PathVariable Long id) {
        return bookService.getBooksByIds(id);
    }

    @GetMapping("/isbn/{isbn}")
    public BookResponseDto getBookByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }
}
