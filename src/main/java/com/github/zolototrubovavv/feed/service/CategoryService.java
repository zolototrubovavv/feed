package com.github.zolototrubovavv.feed.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.zolototrubovavv.feed.dto.CategoryDto;
import com.github.zolototrubovavv.feed.dto.CategoryRequestDto;
import com.github.zolototrubovavv.feed.entity.Category;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.exception.category.CategoryNameAlreadyExistsException;
import com.github.zolototrubovavv.feed.exception.category.CategoryNotFoundException;
import com.github.zolototrubovavv.feed.exception.category.CategoryServiceException;
import com.github.zolototrubovavv.feed.mapper.CategoryMapper;
import com.github.zolototrubovavv.feed.repository.CategoryRepository;
import com.github.zolototrubovavv.feed.repository.NewsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final NewsRepository newsRepository;
    private final CategoryMapper categoryMapper;

    @Cacheable(value = "category", key = "'all'")
    public List<CategoryDto> getAll() {
        log.info("Fetching all categories");
        try {
            List<Category> categories = categoryRepository.findAll();
            List<CategoryDto> dtos = categories.stream()
                .map(category -> categoryMapper.toDto(category))
                .collect(Collectors.toList());
            
            log.info("Successfully fetched all categories | Total count: {}", dtos.size());
            return dtos;
        } catch (Exception e) {
            log.error("Failed to fetch all categories | Critical error", e);
            throw new CategoryServiceException("Failed to retrieve categories", e);
        }
    }

    @Cacheable(value = "category", key = "#id")
    public CategoryDto getById(Long id) {
        log.info("Fetching category by ID -> Target ID: {}", id);
        try {
            Category category = getByIdOrElseThrow(id);
            log.info("Successfully fetched category by ID -> Target ID: {} | Name: '{}'", id, category.getName());
            return categoryMapper.toDto(category);
        } catch (CategoryNotFoundException e) {
            log.warn("Failed to fetch category by ID -> Target ID: {} | Reason: Not found", id);
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Failed to fetch category by ID -> Target ID: {} | Reason: Invalid ID validation", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch category by ID -> Target ID: {} | Critical error", id, e);
            throw new CategoryServiceException("Failed to retrieve category", e);
        }
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "category", key = "#result.id"),
            @CacheEvict(value = "category", key = "'all'")
        }
    )
    public CategoryDto create(CategoryRequestDto categoryDto) {
        log.info("Creating new category -> Request context: {}", categoryDto);
        try {
            String name = categoryDto.name();
            if (categoryRepository.existsByNameIgnoreCase(name, null)) {
                log.warn("Category creation rejected -> Name: '{}' | Reason: Name already exists", name);
                throw new CategoryNameAlreadyExistsException(name);
            }

            Category categoryToCreate = categoryMapper.fromCreateRequest(categoryDto);
            Category result = categoryRepository.save(categoryToCreate);

            log.info("Successfully created new category -> Created ID: {} | Name: '{}'", result.getId(), result.getName());
            return categoryMapper.toDto(result);

        } catch (CategoryNameAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create category -> Request context: {} | Critical error", categoryDto, e);
            throw e;
        }
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "category", key = "'all'")
        }
    )
    public CategoryDto update(Long id, CategoryRequestDto categoryDto) {
        log.info("Updating category -> Target ID: {}, Request context: {}", id, categoryDto);
        try {
            String name = categoryDto.name();
            if (categoryRepository.existsByNameIgnoreCase(name, id)) {
                log.warn("Category update rejected -> Target ID: {} | Reason: Name '{}' already exists", id, name);
                throw new CategoryNameAlreadyExistsException(name);
            }
            
            Category categoryToUpdate = getByIdOrElseThrow(id);
            categoryMapper.updateEntityFromDto(categoryDto, categoryToUpdate);
            Category result = categoryRepository.save(categoryToUpdate);

            log.info("Successfully updated category -> Target ID: {} | New name: '{}'", result.getId(), result.getName());
            return categoryMapper.toDto(result);

        } catch (CategoryNotFoundException | CategoryNameAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update category -> Target ID: {} | Critical error", id, e);
            throw e;
        }
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "category", key = "'all'")
        }
    )
    public void delete(Long id) {
        log.info("Deleting category -> Target ID: {}", id);
        try {
            Category existedCategory = getByIdOrElseThrow(id);
            List<News> linkedNews = newsRepository.findByCategory_Id(id);
            if (!linkedNews.isEmpty()) {
                log.warn("Category deletion rejected -> Target ID: {} | Reason: Has {} linked news records", id, linkedNews.size());
                throw new RuntimeException(
                    String.format(
                        "Category %s has news with ids=[%s]",
                        existedCategory.getName(),
                        linkedNews.stream()
                            .map(news -> news.getId().toString())
                            .collect(Collectors.joining(", "))
                    )
                );
            }

            categoryRepository.deleteById(id);
            log.info("Successfully deleted category -> Target ID: {}", id);

        } catch (CategoryNotFoundException e) {
            log.warn("Failed to delete category -> Target ID: {} | Reason: Not found", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete category -> Target ID: {} | Critical error", id, e);
            throw e;
        }
    }

    public Category getByIdOrElseThrow(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category id: " + id);
        }

        return categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
