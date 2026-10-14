package com.github.zolototrubovavv.feed.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestDto;
import com.github.zolototrubovavv.feed.dto.NewsRequestPatchDto;
import com.github.zolototrubovavv.feed.dto.PageResponse;
import com.github.zolototrubovavv.feed.entity.Category;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.enums.NewsStatus;
import com.github.zolototrubovavv.feed.exception.news.NewsNotFoundException;
import com.github.zolototrubovavv.feed.exception.news.NewsServiceException;
import com.github.zolototrubovavv.feed.kafka.KafkaNewsProducer;
import com.github.zolototrubovavv.feed.mapper.NewsMapper;
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
    private final KafkaNewsProducer kafkaProducer;
    private final NewsCacheService newsCacheService;

    private static final int MAX_PAGE_SIZE = 100;

    public PageResponse<NewsDto> getNewsPage(Pageable pageable) {
        int safeSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        log.info("Fetching news page -> Page: {}, Size: {}", pageable.getPageNumber(), safeSize);
        try {
            PageResponse<NewsDto> response = newsCacheService.getNewsPage(pageable.getPageNumber(), safeSize);
            log.info("Successfully fetched news page -> Page: {} | Total elements: {}", pageable.getPageNumber(), response.getTotalElements());
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch news page -> Page: {} | Critical error", pageable.getPageNumber(), e);
            throw new NewsServiceException("Failed to retrieve news list", e);
        }
    }

    public List<NewsDto> getLatest() {
        log.info("Fetching latest news list");
        try {
            List<NewsDto> latestNews = newsCacheService.getLatestNews();
            log.info("Successfully fetched latest news list | Total count: {}", latestNews.size());
            return latestNews;
        } catch (Exception e) {
            log.error("Failed to fetch latest news list | Critical error", e);
            throw new NewsServiceException("Failed to retrieve news list", e);
        }
    }

    public NewsDto getById(Long id) {
        log.info("Fetching news by ID -> Target ID: {}", id);
        try {
            NewsDto newsDto = newsCacheService.getNewsById(id);
            log.info("Successfully fetched news by ID -> Target ID: {} | Title: '{}'", id, newsDto.getTitle());
            return newsDto;
        } catch (NewsNotFoundException e) {
            log.warn("Failed to fetch news by ID -> Target ID: {} | Reason: Not found", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch news by ID -> Target ID: {} | Critical error", id, e);
            throw new NewsServiceException("Failed to retrieve news", e);
        }
    }

    @Transactional
    public NewsDto create(NewsRequestDto newsDto) {
        log.info("Creating new news -> Request context: {}", newsDto);
        try {
            News newsToCreate = newsMapper.fromCreateRequest(newsDto);
            Category category = categoryService.getByIdOrElseThrow(newsDto.categoryId());

            newsToCreate.setStatus(NewsStatus.DRAFT);
            newsToCreate.setCategory(category);

            News result = newsRepository.save(newsToCreate);
            kafkaProducer.sendNewsCreated(result);

            log.info("Successfully created new news -> Created ID: {} | Title: '{}'", result.getId(), result.getTitle());
            return newsMapper.toDto(result);

        } catch (Exception e) {
            log.error("Failed to create news -> Request context: {} | Critical error", newsDto, e);
            throw e;
        }
    }

    @Transactional
    public NewsDto update(Long id, NewsRequestDto newsDto) {
        log.info("Updating news -> Target ID: {}, Request context: {}", id, newsDto);
        try {
            News newsToUpdate = getByIdOrElseThrow(id);
            newsMapper.updateEntityFromDto(newsDto, newsToUpdate);

            if (!newsToUpdate.getCategory().getId().equals(newsDto.categoryId())) {
                Category category = categoryService.getByIdOrElseThrow(newsDto.categoryId());
                newsToUpdate.setCategory(category);
            }
            
            News result = newsRepository.save(newsToUpdate);
            kafkaProducer.sendNewsUpdated(result);

            log.info("Successfully updated news -> Target ID: {} | Title: '{}'", result.getId(), result.getTitle());
            return newsMapper.toDto(result);

        } catch (NewsNotFoundException e) {
            log.warn("Failed to update news -> Target ID: {} | Reason: News not found", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update news -> Target ID: {} | Critical error", id, e);
            throw e;
        }
    }

    @Transactional
    public NewsDto updatePatch(Long id, NewsRequestPatchDto newsDto) {
        log.info("Patching news partly -> Target ID: {}, Request context: {}", id, newsDto);
        try {
            News newsToUpdate = getByIdOrElseThrow(id);

            if (newsDto.categoryId() != null && !newsToUpdate.getCategory().getId().equals(newsDto.categoryId())) {
                Category category = categoryService.getByIdOrElseThrow(newsDto.categoryId());
                newsToUpdate.setCategory(category);
            }

            if (newsDto.title() != null) {
                newsToUpdate.setTitle(newsDto.title());
            }

            if (newsDto.content() != null) {
                newsToUpdate.setContent(newsDto.content());
            }
            
            News result = newsRepository.save(newsToUpdate);
            kafkaProducer.sendNewsUpdated(result);

            log.info("Successfully patched news partly -> Target ID: {} | Title: '{}'", result.getId(), result.getTitle());
            return newsMapper.toDto(result);

        } catch (NewsNotFoundException e) {
            log.warn("Failed to patch news partly -> Target ID: {} | Reason: News not found", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to patch news partly -> Target ID: {} | Critical error", id, e);
            throw e;
        }
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting news -> Target ID: {}", id);
        try {
            getByIdOrElseThrow(id);
            newsRepository.deleteById(id);
            kafkaProducer.sendNewsDeleted(id);

            log.info("Successfully deleted news -> Target ID: {}", id);

        } catch (NewsNotFoundException e) {
            log.warn("Failed to delete news -> Target ID: {} | Reason: Not found", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete news -> Target ID: {} | Critical error", id, e);
            throw e;
        }
    }

    @Transactional
    public NewsDto publish(Long id) {
        log.info("Publishing news -> Target ID: {}", id);
        try {
            News newsToUpdate = getByIdOrElseThrow(id);
            newsToUpdate.setStatus(NewsStatus.PUBLISHED);
            newsToUpdate.setPublishedAt(LocalDateTime.now());
            
            News result = newsRepository.save(newsToUpdate);
            kafkaProducer.sendNewsPublished(result);

            log.info("Successfully published news -> Target ID: {} | Title: '{}' | Published at: {}", result.getId(), result.getTitle(), result.getPublishedAt());
            return newsMapper.toDto(result);

        } catch (NewsNotFoundException e) {
            log.warn("Failed to publish news -> Target ID: {} | Reason: Not found", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to publish news -> Target ID: {} | Critical error", id, e);
            throw e;
        }
    }

    public News getByIdOrElseThrow(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid news id: " + id);
        }

        return newsRepository.findById(id)
            .orElseThrow(() -> new NewsNotFoundException(id));
    }
}
