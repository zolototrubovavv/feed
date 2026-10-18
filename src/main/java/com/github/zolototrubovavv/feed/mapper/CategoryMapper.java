package com.github.zolototrubovavv.feed.mapper;

import com.github.zolototrubovavv.feed.config.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.github.zolototrubovavv.feed.dto.CategoryDto;
import com.github.zolototrubovavv.feed.dto.CategoryRequestDto;
import com.github.zolototrubovavv.feed.entity.Category;

// Пока тут не много параметров конфигурирования, но если понадобится больше, то можно в конфиг вынести

// Конфиг я добавил, но не подключал. Для подключения поменяй в обоих мапперах @Mapper(componentModel = "spring") на @Mapper(config = MapStructConfig.class)

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    // INSTANCE тут не нужен
    // такой же INSTANCE есть в NewsMapper - стоит убрать в обоих.
    // При componentModel = "spring" маппер - обычный бин, а Mappers.getMapper() создаёт второй, не управляемый Spring экземпляр
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDto toDto(Category category);
    
    Category fromDto(CategoryDto dto);
    
    @Mapping(target = "id", ignore = true)
    Category fromCreateRequest(CategoryRequestDto dto);

    Category fromUpdateRequest(CategoryRequestDto dto, Long id);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CategoryRequestDto dto, @MappingTarget Category existedEntity);
}
