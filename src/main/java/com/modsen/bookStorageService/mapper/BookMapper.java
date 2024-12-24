package com.modsen.bookStorageService.mapper;

import com.modsen.bookStorageService.dto.BookResponseDto;
import com.modsen.bookStorageService.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    BookResponseDto toDto(Book book);

    default Page<BookResponseDto> toDtoPage(Page<Book> bookPage) {
        return bookPage.map(this::toDto);
    }
}