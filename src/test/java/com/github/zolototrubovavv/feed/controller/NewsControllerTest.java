package com.github.zolototrubovavv.feed.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.zolototrubovavv.feed.config.SecurityConfig;
import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.PageResponse;
import com.github.zolototrubovavv.feed.exception.GlobalExceptionHandler;
import com.github.zolototrubovavv.feed.service.NewsService;

@WebMvcTest(NewsController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Web MVC Tests for NewsController")
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsService newsService;

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @MockitoBean 
    private org.springframework.cache.CacheManager cacheManager;

    @Test
    @DisplayName("Should return paginated news")
    void shouldGetNewsPageSuccessfully(CapturedOutput output) throws Exception {
        PageResponse<NewsDto> mockPageResponse = new PageResponse<>(
                Collections.emptyList(), 0, 10, 0L, 0, true
        );
        Mockito.when(newsService.getNewsPage(ArgumentMatchers.any(Pageable.class))).thenReturn(mockPageResponse);

        mockMvc.perform(get("/api/news")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser("test-user")
    @DisplayName("Should return 400 BAD REQUEST when trying to send invalid body")
    void shouldReturn400WhenValidationFails(CapturedOutput output) throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/api/news")
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("One or more fields are invalid"));
    }
}
