package com.modsen.bookStorageService.dto;

public record BookDto(Long id, String isbn, String title, String genre, String description, String author) {
}

