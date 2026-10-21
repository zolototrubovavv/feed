package com.github.zolototrubovavv.feed.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    @DisplayName("Should fetch news from DB and write to cache")
    void shouldFetchNewsFromDbAndCacheIt() {
        Long newsId = 1L;
        News mockNews = new News();
        mockNews.setId(newsId);
        mockNews.setTitle("Test Title");
        
        Mockito.when(newsRepository.findById(newsId)).thenReturn(Optional.of(mockNews));

        NewsDto firstCallResult = newsCacheService.getNewsById(newsId);

        assertThat(firstCallResult).isNotNull();
        assertThat(redisTemplate.opsForValue().get("news::id::1")).isNotNull();
        
        NewsDto secondCallResult = newsCacheService.getNewsById(newsId);

        assertThat(secondCallResult).isNotNull();
        Mockito.verify(newsRepository, Mockito.times(1)).findById(newsId); 
    }

    @Test
    @DisplayName("Should evict entity cache and trigger full lists eviction when refreshNewsCache is called")
    void shouldEvictCacheAndLogProperChain() {
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
    }
}
