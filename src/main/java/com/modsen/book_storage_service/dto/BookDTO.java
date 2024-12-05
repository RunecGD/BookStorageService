package com.modsen.book_storage_service.dto;

import lombok.Data;

@Data
public class BookDTO {
    private String isbn;
    private String title;
    private String genre;
    private String description;
    private String author;
}
