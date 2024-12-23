package com.modsen.bookStorageService.mapper;

import com.modsen.bookStorageService.dto.ResponseDto;
import com.modsen.bookStorageService.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    ResponseDto toDto(Book book);

    default Page<ResponseDto> toDtoPage(Page<Book> bookPage) {
        return bookPage.map(this::toDto);
    }
}