package com.modsen.bookStorageService.exception;

public class BookAlreadyTakenException extends RuntimeException {
    public BookAlreadyTakenException(String message) {
        super(message);
    }
}
