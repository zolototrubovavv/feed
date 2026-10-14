package com.github.zolototrubovavv.feed.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.repository.NewsRepository;

@SpringBootTest
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Integration Tests for NewsCacheService")
class NewsCacheServiceIT {

    @Container
    private static final GenericContainer<?> redisContainer = 
            new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine")).withExposedPorts(6379);

    @Autowired
    private NewsCacheService newsCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private NewsRepository newsRepository;

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("Should fetch news from DB, write to cache, and output correct log format")
    void shouldFetchNewsFromDbAndCacheIt(CapturedOutput output) {
        Long newsId = 1L;
        News mockNews = new News();
        mockNews.setId(newsId);
        mockNews.setTitle("Test Title");
        
        Mockito.when(newsRepository.findById(newsId)).thenReturn(Optional.of(mockNews));

        NewsDto firstCallResult = newsCacheService.getNewsById(newsId);

        assertThat(firstCallResult).isNotNull();
        assertThat(redisTemplate.opsForValue().get("news::id::1")).isNotNull();
        
        assertThat(output.getOut()).contains("Fetching news by ID -> Source: Cache, Target ID: 1");
        assertThat(output.getOut()).contains("Successfully fetched news by ID -> Target ID: 1 | Source: DB (Cache missed)");

        NewsDto secondCallResult = newsCacheService.getNewsById(newsId);

        assertThat(secondCallResult).isNotNull();
        Mockito.verify(newsRepository, Mockito.times(1)).findById(newsId); 
        
        assertThat(output.getOut()).contains("Successfully fetched news by ID -> Target ID: 1 | Source: Cache");
    }

    @Test
    @DisplayName("Should evict entity cache and trigger full lists eviction when refreshNewsCache is called")
    void shouldEvictCacheAndLogProperChain(CapturedOutput output) {
        Long newsId = 42L;
        String entityKey = "news::id::42";
        String pageKey = "news::page::0::size::10";
        
        News mockNews = new News();
        mockNews.setId(newsId);
        mockNews.setTitle("Updated Title");
        Mockito.when(newsRepository.findById(newsId)).thenReturn(Optional.of(mockNews));

        redisTemplate.opsForValue().set(entityKey, "old_data");
        redisTemplate.opsForValue().set(pageKey, "page_data");

        newsCacheService.refreshNewsCache(newsId);

        assertThat(redisTemplate.opsForValue().get(pageKey)).isNull();
        assertThat(redisTemplate.opsForValue().get(entityKey)).isNotEqualTo("old_data");

        assertThat(output.getOut()).contains("Executing entity cache eviction -> Target ID: 42");
        assertThat(output.getOut()).contains("Successfully evicted entity cache -> Target ID: 42 | Proceeding to list eviction");
        assertThat(output.getOut()).contains("Executing full lists cache eviction");
        assertThat(output.getOut()).contains("Deleting cache keys by pattern -> Pattern: news::page::*::size::*");
        assertThat(output.getOut()).contains("Successfully deleted cache keys by pattern");
    }
}
