package com.modsen.bookStorageService.dto;

public record BookDTO(String isbn, String title, String genre, String description, String author) {
}
