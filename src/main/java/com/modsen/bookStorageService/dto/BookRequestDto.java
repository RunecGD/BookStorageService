package com.modsen.bookStorageService.dto;

public record BookRequestDto(Long id, String isbn, String title, String genre, String description, String author) {
}
