package com.github.zolototrubovavv.feed.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.zolototrubovavv.feed.dto.CategoryDto;
import com.github.zolototrubovavv.feed.dto.CategoryRequestDto;
import com.github.zolototrubovavv.feed.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<CategoryDto>> allCategories() {
        log.info("REST request -> Method: GET, Path: /api/categories");
        
        List<CategoryDto> categories = categoryService.getAll();
        
        log.info("REST response -> Method: GET, Path: /api/categories | Status: 200 OK, Elements returned: {}", categories.size());
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        log.info("REST request -> Method: GET, Path: /api/categories/{}", id);
        
        CategoryDto categoryDto = categoryService.getById(id);
        
        log.info("REST response -> Method: GET, Path: /api/categories/{} | Status: 200 OK, Name: '{}'", id, categoryDto.getName());
        return ResponseEntity.ok(categoryDto);
    }
    
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody @Valid CategoryRequestDto categoryDto) {
        log.info("REST request -> Method: POST, Path: /api/categories, Request context: {}", categoryDto);
        
        CategoryDto result = categoryService.create(categoryDto);
        
        log.info("REST response -> Method: POST, Path: /api/categories | Status: 200 OK, Created ID: {} | Name: '{}'", result.getId(), result.getName());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
        @PathVariable Long id,
        @RequestBody @Valid CategoryRequestDto categoryDto
    ) {
        log.info("REST request -> Method: PUT, Path: /api/categories/{}, Request context: {}", id, categoryDto);

        CategoryDto result = categoryService.update(id, categoryDto);
        
        log.info("REST response -> Method: PUT, Path: /api/categories/{} | Status: 200 OK, Updated ID: {} | New name: '{}'", id, result.getId(), result.getName());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("REST request -> Method: DELETE, Path: /api/categories/{}", id);

        categoryService.delete(id);
        
        log.info("REST response -> Method: DELETE, Path: /api/categories/{} | Status: 24 No Content", id);
        return ResponseEntity.noContent().build();
    }
}