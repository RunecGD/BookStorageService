package com.modsen.bookStorageService.dto;

public record BookDto(String isbn, String title, String genre, String description, String author) {
}
