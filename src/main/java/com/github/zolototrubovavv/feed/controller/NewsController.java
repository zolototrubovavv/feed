package com.github.zolototrubovavv.feed.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.zolototrubovavv.feed.constants.NewsConstants;
import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPatchDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPutDto;
import com.github.zolototrubovavv.feed.dto.PageResponse;
import com.github.zolototrubovavv.feed.service.NewsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<NewsDto> getNewsPage(@PageableDefault(page = 0, size = NewsConstants.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return newsService.getNewsPage(pageable);
    }

    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NewsDto> getLatestNews() {
        return newsService.getLatest();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NewsDto getNewsById(@PathVariable Long id) {
        return newsService.getById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NewsDto createNews(@RequestBody @Valid NewsRequestPutDto newsDto) {
        return newsService.create(newsDto);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public NewsDto updateNews(@PathVariable Long id, @RequestBody @Valid NewsRequestPutDto newsDto) {
        return newsService.updatePut(id, newsDto);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public NewsDto updateNews(@PathVariable Long id, @RequestBody @Valid NewsRequestPatchDto newsDto) {
        return newsService.updatePatch(id, newsDto);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNews(@PathVariable Long id) {
        newsService.delete(id);
    }

    @PostMapping(value = "/{id}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
    public NewsDto publishNewsById(@PathVariable Long id) {
        return newsService.publish(id);
    }
}