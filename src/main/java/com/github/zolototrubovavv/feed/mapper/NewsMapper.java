package com.github.zolototrubovavv.feed.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestDto;
import com.github.zolototrubovavv.feed.entity.News;

@Mapper(componentModel = "spring")
public interface NewsMapper {

    NewsMapper INSTANCE = Mappers.getMapper(NewsMapper.class);

    NewsDto toDto(News news);
    
    News fromDto(NewsDto dto);
    
    @Mapping(target = "id", ignore = true)
    News fromCreateRequest(NewsRequestDto dto);

    News fromUpdateRequest(NewsRequestDto dto, Long id);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(NewsRequestDto dto, @MappingTarget News existedEntity);
}
