package com.github.zolototrubovavv.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.zolototrubovavv.feed.dto.CategoryDto;
import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.entity.Category;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.enums.NewsStatus;
import com.github.zolototrubovavv.feed.kafka.KafkaNewsProducer;
import com.github.zolototrubovavv.feed.kafka.NewsEvent;
import com.github.zolototrubovavv.feed.kafka.NewsEventType;
import com.github.zolototrubovavv.feed.mapper.NewsEventMapper;
import com.github.zolototrubovavv.feed.mapper.NewsMapper;
import com.github.zolototrubovavv.feed.repository.NewsRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for NewsService")
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private NewsEventMapper newsEventMapper;

    @Mock
    private KafkaNewsProducer kafkaProducer;

    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("Should successfully change status to PUBLISHED and send Kafka event")
    void shouldPublishNewsSuccessfully() {
        Long newsId = 77L;
        
        Category category = new Category();
        category.setId(1L);
        category.setName("Some category");

        News existingNews = new News();
        existingNews.setId(newsId);
        existingNews.setTitle("Breaking News");
        existingNews.setStatus(NewsStatus.DRAFT);
        existingNews.setCategory(category);

        News savedNews = new News();
        savedNews.setId(newsId);
        savedNews.setTitle("Breaking News");
        savedNews.setStatus(NewsStatus.PUBLISHED);
        savedNews.setCategory(category);

        NewsDto expectedDto = NewsDto.builder()
                .id(newsId)
                .title("Breaking News")
                .content("Content")
                .status(NewsStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .category(new CategoryDto(1L, "Some category"))
                .build();

        NewsEvent publishEvent = new NewsEvent(
                NewsEventType.PUBLISHED, 
                newsId, 
                "Breaking News", 
                "Content", 
                1L, 
                LocalDateTime.now()
        );

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(any(News.class))).thenReturn(savedNews);
        when(newsMapper.toDto(savedNews)).thenReturn(expectedDto);
        when(newsEventMapper.toEvent(any(News.class), eq(NewsEventType.PUBLISHED))).thenReturn(publishEvent);

        NewsDto result = newsService.publish(newsId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NewsStatus.PUBLISHED);

        verify(kafkaProducer, times(1)).sendEvent(publishEvent);
        verify(newsRepository, times(1)).save(existingNews);
    }
}
