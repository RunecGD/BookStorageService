package com.modsen.bookStorageService.mapper;

import com.modsen.bookStorageService.dto.BookResponseDto;
import com.modsen.bookStorageService.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);
    @Mapping(source = "id", target = "id")
    @Mapping(source = "isbn", target = "isbn")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "genre", target = "genre")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "author", target = "author")
    BookResponseDto toDto(Book book);

    default Page<BookResponseDto> toDtoPage(Page<Book> bookPage) {
        return bookPage.map(this::toDto);
    }
}