package com.github.zolototrubovavv.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.zolototrubovavv.feed.dto.CategoryDto;
import com.github.zolototrubovavv.feed.dto.CategoryRequestDto;
import com.github.zolototrubovavv.feed.entity.Category;
import com.github.zolototrubovavv.feed.exception.category.CategoryNameAlreadyExistsException;
import com.github.zolototrubovavv.feed.mapper.CategoryMapper;
import com.github.zolototrubovavv.feed.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for CategoryService")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should successfully create a category")
    void shouldCreateCategorySuccessfully() {
        CategoryRequestDto requestDto = new CategoryRequestDto("Finance");
        Category entityToCreate = new Category();
        entityToCreate.setName("Finance");
        
        Category savedEntity = new Category();
        savedEntity.setId(10L);
        savedEntity.setName("Finance");

        CategoryDto expectedDto = new CategoryDto(10L, "Finance");

        when(categoryRepository.existsByNameIgnoreCase("Finance", null)).thenReturn(false);
        when(categoryMapper.fromCreateRequest(requestDto)).thenReturn(entityToCreate);
        when(categoryRepository.save(entityToCreate)).thenReturn(savedEntity);
        when(categoryMapper.toDto(savedEntity)).thenReturn(expectedDto);

        CategoryDto result = categoryService.create(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Finance");
        
        verify(categoryRepository, times(1)).save(entityToCreate);
    }

    @Test
    @DisplayName("Should throw exception when category name already exists")
    void shouldThrowExceptionWhenNameExists() {
        CategoryRequestDto requestDto = new CategoryRequestDto("Finance");
        when(categoryRepository.existsByNameIgnoreCase("Finance", null)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(requestDto))
                .isInstanceOf(CategoryNameAlreadyExistsException.class);
        
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
