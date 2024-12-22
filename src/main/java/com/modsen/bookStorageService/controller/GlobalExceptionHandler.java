package com.modsen.bookStorageService.controller;

import com.modsen.bookStorageService.exception.BookAlreadyTakenException;
import com.modsen.bookStorageService.exception.BookNotFoundException;
import com.modsen.bookStorageService.exception.BookNotTakenException;
import com.modsen.bookStorageService.exception.ISBNAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<String> handleBookNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({BookAlreadyTakenException.class, ISBNAlreadyExistsException.class, BookNotTakenException.class})
    public ResponseEntity<String> handleConflictExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}

