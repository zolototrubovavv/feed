package com.github.zolototrubovavv.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.github.zolototrubovavv.feed.dto.NewsDto;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.enums.NewsStatus;
import com.github.zolototrubovavv.feed.kafka.KafkaNewsProducer;
import com.github.zolototrubovavv.feed.mapper.NewsMapper;
import com.github.zolototrubovavv.feed.repository.NewsRepository;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@DisplayName("Unit Tests for NewsService")
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private KafkaNewsProducer kafkaProducer;

    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("Should successfully change status to PUBLISHED, send Kafka event, and output logs")
    void shouldPublishNewsSuccessfully(CapturedOutput output) {
        Long newsId = 77L;
        News existingNews = new News();
        existingNews.setId(newsId);
        existingNews.setTitle("Breaking News");
        existingNews.setStatus(NewsStatus.DRAFT);

        News savedNews = new News();
        savedNews.setId(newsId);
        savedNews.setTitle("Breaking News");
        savedNews.setStatus(NewsStatus.PUBLISHED);

        NewsDto expectedDto = NewsDto.builder()
                .id(newsId)
                .title("Breaking News")
                .content("Content")
                .status(NewsStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(any(News.class))).thenReturn(savedNews);
        when(newsMapper.toDto(savedNews)).thenReturn(expectedDto);

        NewsDto result = newsService.publish(newsId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NewsStatus.PUBLISHED);

        verify(kafkaProducer, times(1)).sendNewsPublished(any(News.class));
        verify(newsRepository, times(1)).save(existingNews);

        String logs = output.getOut();
        assertThat(logs).contains("Publishing news -> Target ID: 77");
        assertThat(logs).contains("Successfully published news -> Target ID: 77 | Title: 'Breaking News' | Published at:");
    }
}
