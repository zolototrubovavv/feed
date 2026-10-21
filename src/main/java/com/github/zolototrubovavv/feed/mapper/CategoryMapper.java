package com.github.zolototrubovavv.feed.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.github.zolototrubovavv.feed.config.MapStructConfig;
import com.github.zolototrubovavv.feed.dto.CategoryDto;
import com.github.zolototrubovavv.feed.dto.CategoryRequestDto;
import com.github.zolototrubovavv.feed.entity.Category;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {

    CategoryDto toDto(Category category);
    
    Category fromDto(CategoryDto dto);
    
    @Mapping(target = "id", ignore = true)
    Category fromCreateRequest(CategoryRequestDto dto);

    Category fromUpdateRequest(CategoryRequestDto dto, Long id);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CategoryRequestDto dto, @MappingTarget Category existedEntity);
}
