package com.modsen.bookStorageService.dto;

public record ResponseDto(Long id, String isbn, String title, String genre, String description, String author) {
}
