package com.github.zolototrubovavv.feed.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPutDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPatchDto;
import com.github.zolototrubovavv.feed.dto.PageResponse;
import com.github.zolototrubovavv.feed.entity.Category;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.enums.NewsStatus;
import com.github.zolototrubovavv.feed.exception.news.NewsNotFoundException;
import com.github.zolototrubovavv.feed.exception.news.NewsServiceException;
import com.github.zolototrubovavv.feed.kafka.KafkaNewsProducer;
import com.github.zolototrubovavv.feed.kafka.NewsEvent;
import com.github.zolototrubovavv.feed.kafka.NewsEventType;
import com.github.zolototrubovavv.feed.mapper.NewsMapper;
import com.github.zolototrubovavv.feed.mapper.NewsEventMapper;
import com.github.zolototrubovavv.feed.repository.NewsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final CategoryService categoryService;
    private final NewsMapper newsMapper;
    private final NewsEventMapper newsEventMapper;
    private final KafkaNewsProducer kafkaProducer;
    private final NewsCacheService newsCacheService;

    private static final int MAX_PAGE_SIZE = 100;

    public PageResponse<NewsDto> getNewsPage(Pageable pageable) {
        int safeSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        return newsCacheService.getNewsPage(pageable.getPageNumber(), safeSize);
    }

    public List<NewsDto> getLatest() {
        return newsCacheService.getLatestNews();
    }

    public NewsDto getById(Long id) {
        return newsCacheService.getNewsById(id);
    }

    @Transactional
    public NewsDto create(NewsRequestPutDto newsDto) {
        log.info("Creating new news -> Request context: {}", newsDto);
        News newsToCreate = newsMapper.fromCreateRequest(newsDto);
        Category category = categoryService.getByIdOrElseThrow(newsDto.categoryId());

        newsToCreate.setStatus(NewsStatus.DRAFT);
        newsToCreate.setCategory(category);

        News result = newsRepository.save(newsToCreate);
        handleEvent(result, NewsEventType.CREATED);

        log.info("Successfully created new news -> Created ID: {} | Title: '{}'", result.getId(),
                result.getTitle());
        return newsMapper.toDto(result);
    }

    @Transactional
    public NewsDto updatePut(Long id, NewsRequestPutDto newsDto) {
        log.info("Updating news -> Target ID: {}, Request context: {}", id, newsDto);
        News newsToUpdate = getByIdOrElseThrow(id);
        newsMapper.updateEntityFromDtoForPut(newsDto, newsToUpdate);

        if (!newsToUpdate.getCategory().getId().equals(newsDto.categoryId())) {
            Category category = categoryService.getByIdOrElseThrow(newsDto.categoryId());
            newsToUpdate.setCategory(category);
        }

        News result = newsRepository.save(newsToUpdate);
        handleEvent(result, NewsEventType.UPDATED);

        log.info("Successfully updated news -> Target ID: {} | Title: '{}'", result.getId(), result.getTitle());
        return newsMapper.toDto(result);
    }

    @Transactional
    public NewsDto updatePatch(Long id, NewsRequestPatchDto newsDto) {
        log.info("Patching news partly -> Target ID: {}, Request context: {}", id, newsDto);
        News newsToUpdate = getByIdOrElseThrow(id);
        newsMapper.updateEntityFromDtoForPatch(newsDto, newsToUpdate);

        News result = newsRepository.save(newsToUpdate);
        handleEvent(result, NewsEventType.UPDATED);

        log.info("Successfully patched news partly -> Target ID: {} | Title: '{}'", result.getId(),
                result.getTitle());
        return newsMapper.toDto(result);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting news -> Target ID: {}", id);
        getByIdOrElseThrow(id);
        newsRepository.deleteById(id);
        handleEvent(id, NewsEventType.DELETED);

        log.info("Successfully deleted news -> Target ID: {}", id);
    }

    @Transactional
    public NewsDto publish(Long id) {
        log.info("Publishing news -> Target ID: {}", id);
        News newsToUpdate = getByIdOrElseThrow(id);
        newsToUpdate.setStatus(NewsStatus.PUBLISHED);
        newsToUpdate.setPublishedAt(LocalDateTime.now());

        News result = newsRepository.save(newsToUpdate);
        handleEvent(result, NewsEventType.PUBLISHED);

        log.info("Successfully published news -> Target ID: {} | Title: '{}' | Published at: {}", result.getId(),
                result.getTitle(), result.getPublishedAt());
        return newsMapper.toDto(result);
    }

    public News getByIdOrElseThrow(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid news id: " + id);
        }

        return newsRepository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException(id));
    }

    private void handleEvent(News news, NewsEventType eventType) {
        try {
            NewsEvent event = newsEventMapper.toEvent(news, eventType);
            kafkaProducer.sendEvent(event);
        } catch (Exception e) {
            newsCacheService.refreshNewsCache(news.getId());
        }
    }

    private void handleEvent(Long newsId, NewsEventType eventType) {
        try {
            NewsEvent event = newsEventMapper.toEvent(newsId, eventType);
            kafkaProducer.sendEvent(event);
        } catch (Exception e) {
            newsCacheService.refreshNewsCache(newsId);
        }
    }
}
