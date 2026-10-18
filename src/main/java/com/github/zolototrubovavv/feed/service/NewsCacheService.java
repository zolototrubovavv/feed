package com.github.zolototrubovavv.feed.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.dto.PageResponse;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.enums.NewsConstants;
import com.github.zolototrubovavv.feed.exception.news.NewsNotFoundException;
import com.github.zolototrubovavv.feed.exception.news.NewsServiceException;
import com.github.zolototrubovavv.feed.mapper.NewsMapper;
import com.github.zolototrubovavv.feed.repository.NewsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // с 4 версии Spring Boot стоит использовать JsonMapper
    //  @ConditionalOnMissingBean в Boot 4 стоит на JsonMapper. Если кто-то позже объявит свой @Bean ObjectMapper -
    //  автоконфигурация не отключится, и в контексте окажутся два маппера с разными настройками.
    //  В Spring Boot 3 условие стояло на ObjectMapper
    private final NewsMapper newsMapper;
    private final NewsRepository newsRepository;

    private static final String NEWS_LATEST_KEY = "news::latest";
    private static final String NEWS_ID_PATTERN = "news::id::%s";
    private static final String NEWS_PAGE_PATTERN = "news::page::%s::size::%s";

    public List<NewsDto> getLatestNews() {
        log.info("Fetching latest news -> Source: Cache");
        try {
            List<NewsDto> cacheData = getDataFromCache(NEWS_LATEST_KEY);
            if (cacheData != null) {
                log.info("Successfully fetched latest news -> Source: Cache | Count: {}", cacheData.size());
                return cacheData;
            }

            List<NewsDto> dbData = getLatestNewsFromDb();
            putDataToCache(NEWS_LATEST_KEY, dbData);

            log.info("Successfully fetched latest news -> Source: DB (Cache missed) | Count: {}", dbData.size());
            return dbData;
        } catch (Exception e) {
            log.error("Failed to fetch latest news from cache | Critical error", e);
            throw new NewsServiceException("Failed to retrieve news", e);
        }
    }

    private List<NewsDto> getLatestNewsFromDb() {
        log.info("Fetching latest news -> Source: DB");
        try {
            List<News> newsList = newsRepository.findLatest10Published();
            List<NewsDto> result = newsList.stream()
                .map(news -> newsMapper.toDto(news))
                .collect(Collectors.toList());

            log.info("Successfully fetched latest news from DB | Count: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch latest news from DB | Critical error", e);
        } 
        return Collections.emptyList();
    }

    private <T> void putDataToCache(String key, T data) {
        log.info("Writing data to cache -> Key: {}", key);
        try {
            redisTemplate.opsForValue().set(key, data);
            log.info("Successfully wrote data to cache -> Key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to write data to cache -> Key: {} | Reason: Cache provider error", key, e);
        }
    }
    
    private <T> void putDataToCache(String key, T data, long timeout, TimeUnit unit) {
        log.info("Writing data to cache with TTL -> Key: {}, Timeout: {}, Unit: {}", key, timeout, unit);
        try {
            redisTemplate.opsForValue().set(key, data, timeout, unit);
            log.info("Successfully wrote data to cache with TTL -> Key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to write data to cache with TTL -> Key: {} | Reason: Cache provider error", key, e);
        }
    }

    private <T> T getDataFromCache(String key) {
        log.info("Reading data from cache -> Key: {}", key);
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw != null) {
                T converted = objectMapper.convertValue(raw, new TypeReference<T>() {});
                log.info("Successfully read data from cache -> Key: {} | Status: Hit", key);
                return converted;
            }
            log.info("Successfully read data from cache -> Key: {} | Status: Miss", key);
        } catch (Exception e) {
            log.warn("Failed to read data from cache -> Key: {} | Reason: Deserialization or connection error", key, e);
        }
        return null;
    }

    public PageResponse<NewsDto> getNewsPage(int page, int size) {
        log.info("Fetching news page -> Source: Cache, Page: {}, Size: {}", page, size);
        try {
            String pageCacheKey = String.format(NEWS_PAGE_PATTERN, page, size);
            PageResponse<NewsDto> cacheData = getDataFromCache(pageCacheKey);

            if (cacheData != null) {
                log.info("Successfully fetched news page -> Source: Cache, Page: {} | Total elements: {}", page, cacheData.getTotalElements());
                return cacheData;
            }

            PageResponse<NewsDto> pageFromDb = getNewsPageFromDb(page, size);
            putDataToCache(pageCacheKey, pageFromDb, 15, TimeUnit.MINUTES);

            log.info("Successfully fetched news page -> Source: DB (Cache missed), Page: {} | Total elements: {}", page, pageFromDb.getTotalElements());
            return pageFromDb;
        } catch (Exception e) {
            log.error("Failed to fetch news page from cache -> Page: {}, Size: {} | Critical error", page, size, e);
            throw new NewsServiceException("Failed to retrieve news", e);
        }
    }

    private PageResponse<NewsDto> getNewsPageFromDb(int page, int size) {
        log.info("Fetching news page -> Source: DB, Page: {}, Size: {}", page, size);
        try {
            Page<News> pageFromDb = newsRepository.findAll(
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            );

            PageResponse<NewsDto> response = new PageResponse<>(
                pageFromDb.stream().map(newsMapper::toDto).toList(),
                pageFromDb.getNumber(),
                pageFromDb.getSize(),
                pageFromDb.getTotalElements(),
                pageFromDb.getTotalPages(),
                pageFromDb.isLast()
            );

            log.info("Successfully fetched news page from DB -> Page: {} | Total elements: {}", page, response.getTotalElements());
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch news page from DB -> Page: {}, Size: {} | Critical error", page, size, e);
            throw e;
        }
    }

    public NewsDto getNewsById(Long id) {
        log.info("Fetching news by ID -> Source: Cache, Target ID: {}", id);
        try {
            String key = String.format(NEWS_ID_PATTERN, id);
            NewsDto cached = getDataFromCache(key);
            if (cached != null) {
                log.info("Successfully fetched news by ID -> Target ID: {} | Source: Cache", id);
                return cached;
            }

            News news = getByIdOrElseThrow(id);
            NewsDto result = newsMapper.toDto(news);
            putDataToCache(key, result, 15, TimeUnit.MINUTES);

            log.info("Successfully fetched news by ID -> Target ID: {} | Source: DB (Cache missed)", id);
            return result;

        } catch (NewsNotFoundException e) {
            log.warn("Failed to fetch news by ID -> Target ID: {} | Reason: Not found", id);
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Failed to fetch news by ID -> Target ID: {} | Reason: Invalid ID validation", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch news by ID -> Target ID: {} | Critical error", id, e);
            throw new NewsServiceException("Failed to retrieve news", e);
        }
    }

    public void forceRefreshTopNewsCache() {
        log.info("Executing force cache refresh -> Focus: Top & First page news");
        try {
            List<NewsDto> latestNews = getLatestNewsFromDb();
            putDataToCache(NEWS_LATEST_KEY, latestNews);

            int page = 0;
            PageResponse<NewsDto> firstPageNews = getNewsPageFromDb(page, NewsConstants.DEFAULT_PAGE_SIZE);
            putDataToCache(String.format(NEWS_PAGE_PATTERN, page, NewsConstants.DEFAULT_PAGE_SIZE), firstPageNews);
            
            log.info("Successfully executed force cache refresh | Operations completed");
        } catch (Exception e) {
            log.error("Failed to execute force cache refresh | Critical error", e);
        }
    }

    public void refreshNewsCache(Long id) {
        log.info("Executing entity cache eviction -> Target ID: {}", id);
        try {
            String key = String.format(NEWS_ID_PATTERN, id);
            redisTemplate.delete(key);
            
            News news = getByIdOrElseThrow(id);
            NewsDto result = newsMapper.toDto(news);
            putDataToCache(key, result, 15, TimeUnit.MINUTES);

            log.info("Successfully evicted entity cache -> Target ID: {} | Proceeding to list eviction", id);
            refreshNewsCache();
        } catch (Exception e) {
            log.warn("Failed to evict entity cache -> Target ID: {} | Operation partially failed", id, e);
        }
    }

    public void refreshNewsCache() {
        log.info("Executing full lists cache eviction");
        try {
            deleteByPattern(String.format(NEWS_PAGE_PATTERN, "*", "*"));
            redisTemplate.delete(NEWS_LATEST_KEY);
            
            log.info("Successfully evicted lists keys | Triggering top cache re-warm");
            forceRefreshTopNewsCache();
        } catch (Exception e) {
            log.error("Failed to execute full lists cache eviction | Critical error", e);
        }
    }

    private void deleteByPattern(String pattern) {
        log.info("Deleting cache keys by pattern -> Pattern: {}", pattern);
        try {
            Set<String> keysToDelete = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
            
            try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(
                    connection -> connection.keyCommands().scan(options))) {
                while (cursor.hasNext()) {
                    keysToDelete.add(new String(cursor.next()));
                }
            }

            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.info("Successfully deleted cache keys by pattern -> Pattern: {} | Deleted keys count: {}", pattern, keysToDelete.size());
            } else {
                log.info("Successfully processed pattern delete -> Pattern: {} | No matching keys found", pattern);
            }
        } catch (Exception e) {
            log.error("Failed to delete cache keys by pattern -> Pattern: {} | Critical error", pattern, e);
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