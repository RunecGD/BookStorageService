package com.modsen.bookStorageService.exception;

public class ISBNAlreadyExistsException extends RuntimeException {
    public ISBNAlreadyExistsException(String message) {
        super(message);
    }
}
