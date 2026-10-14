package com.github.zolototrubovavv.feed.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPatchDto;
import com.github.zolototrubovavv.feed.dto.PageResponse;
import com.github.zolototrubovavv.feed.service.NewsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping
    public ResponseEntity<PageResponse<NewsDto>> getNewsPage(
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        log.info("REST request -> Method: GET, Path: /api/news, Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        PageResponse<NewsDto> response = newsService.getNewsPage(pageable);

        log.info("REST response -> Method: GET, Path: /api/news | Status: 200 OK, Total elements: {}, Total pages: {}", 
            response.getTotalElements(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<NewsDto>> getLatestNews() {
        log.info("REST request -> Method: GET, Path: /api/news/latest");
        
        List<NewsDto> latestNews = newsService.getLatest();

        log.info("REST response -> Method: GET, Path: /api/news/latest | Status: 200 OK, Elements returned: {}", latestNews.size());
        return ResponseEntity.ok(latestNews);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NewsDto> getNewsById(@PathVariable Long id) {
        log.info("REST request -> Method: GET, Path: /api/news/{}", id);

        NewsDto newsDto = newsService.getById(id);

        log.info("REST response -> Method: GET, Path: /api/news/{} | Status: 200 OK, Title: '{}'", id, newsDto.getTitle());
        return ResponseEntity.ok(newsDto);
    }
    
    @PostMapping
    public ResponseEntity<NewsDto> createNews(@RequestBody @Valid NewsRequestDto newsDto) {
        log.info("REST request -> Method: POST, Path: /api/news, Request context: {}", newsDto);

        NewsDto result = newsService.create(newsDto);

        log.info("REST response -> Method: POST, Path: /api/news | Status: 200 OK, Created ID: {} | Title: '{}'", result.getId(), result.getTitle());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsDto> updateNews(
        @PathVariable Long id,
        @RequestBody @Valid NewsRequestDto newsDto
    ) {
        log.info("REST request -> Method: PUT, Path: /api/news/{}, Request context: {}", id, newsDto);

        NewsDto result = newsService.update(id, newsDto);

        log.info("REST response -> Method: PUT, Path: /api/news/{} | Status: 200 OK, Updated ID: {} | New title: '{}'", id, result.getId(), result.getTitle());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NewsDto> updateNews(
        @PathVariable Long id,
        @RequestBody @Valid NewsRequestPatchDto newsDto
    ) {
        log.info("REST request -> Method: PATCH, Path: /api/news/{}, Request context: {}", id, newsDto);

        NewsDto result = newsService.updatePatch(id, newsDto);

        log.info("REST response -> Method: PATCH, Path: /api/news/{} | Status: 200 OK, Patched ID: {} | New title: '{}'", id, result.getId(), result.getTitle());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        log.info("REST request -> Method: DELETE, Path: /api/news/{}", id);
        
        newsService.delete(id);
        
        log.info("REST response -> Method: DELETE, Path: /api/news/{} | Status: 204 No Content", id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/publish")
    public ResponseEntity<NewsDto> publishNewsById(@PathVariable Long id) {
        log.info("REST request -> Method: GET, Path: /api/news/{}/publish", id);

        NewsDto newsDto = newsService.publish(id);

        log.info("REST response -> Method: GET, Path: /api/news/{}/publish | Status: 200 OK, Title: '{}', Status: PUBLISHED", id, newsDto.getTitle());
        return ResponseEntity.ok(newsDto);
    }
}