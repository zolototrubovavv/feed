package com.github.zolototrubovavv.feed.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.github.zolototrubovavv.feed.config.MapStructConfig;
import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPutDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPatchDto;
import com.github.zolototrubovavv.feed.entity.News;

@Mapper(config = MapStructConfig.class)
public interface NewsMapper {

    NewsDto toDto(News news);

    News fromDto(NewsDto dto);

    @Mapping(target = "id", ignore = true)
    News fromCreateRequest(NewsRequestPutDto dto);

    News fromUpdateRequest(NewsRequestPutDto dto, Long id);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDtoForPut(NewsRequestPutDto dto, @MappingTarget News existedEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDtoForPatch(NewsRequestPatchDto dto, @MappingTarget News existedEntity);
}
