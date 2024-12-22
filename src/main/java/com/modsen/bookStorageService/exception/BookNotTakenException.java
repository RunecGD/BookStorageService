package com.modsen.bookStorageService.exception;

public class BookNotTakenException extends RuntimeException {
    public BookNotTakenException(String message) {
        super(message);
    }
}
