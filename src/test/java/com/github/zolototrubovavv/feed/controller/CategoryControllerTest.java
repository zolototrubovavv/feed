package com.github.zolototrubovavv.feed.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.zolototrubovavv.feed.config.SecurityConfig;
import com.github.zolototrubovavv.feed.dto.CategoryDto;
import com.github.zolototrubovavv.feed.exception.GlobalExceptionHandler;
import com.github.zolototrubovavv.feed.exception.category.CategoryNotFoundException;
import com.github.zolototrubovavv.feed.service.CategoryService;

@WebMvcTest(CategoryController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Web MVC Tests for CategoryController")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean 
    private org.springframework.cache.CacheManager cacheManager;

    @Test
    @DisplayName("Should return 200 OK and valid log formats on successful GET by ID")
    void shouldGetCategoryByIdSuccessfully(CapturedOutput output) throws Exception {
        Long id = 1L;
        CategoryDto mockDto = new CategoryDto(id, "Technology");
        Mockito.when(categoryService.getById(id)).thenReturn(mockDto);

        mockMvc.perform(get("/api/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Technology"));

        String logOutput = output.getOut();
        assertThat(logOutput).contains("REST request -> Method: GET, Path: /api/categories/1");
        assertThat(logOutput).contains("REST response -> Method: GET, Path: /api/categories/1 | Status: 200 OK, Name: 'Technology'");
    }

    @Test
    @DisplayName("Should return 404 NOT FOUND and log WARN when category does not exist")
    void shouldReturn404WhenCategoryNotFound(CapturedOutput output) throws Exception {
        Long id = 99L;
        Mockito.when(categoryService.getById(id)).thenThrow(new CategoryNotFoundException(id));

        mockMvc.perform(get("/api/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category with id 99 not found"));

        String logOutput = output.getOut();
        assertThat(logOutput).contains("REST request -> Method: GET, Path: /api/categories/99");
        assertThat(logOutput).contains("REST API Exception -> Path: /api/categories/99, Method: GET | Resource Not Found: Category with id 99 not found");
    }
}
