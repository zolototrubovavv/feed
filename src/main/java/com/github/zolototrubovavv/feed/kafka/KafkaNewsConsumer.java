package com.github.zolototrubovavv.feed.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.github.zolototrubovavv.feed.service.NewsCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaNewsConsumer {

    private final NewsCacheService newsCacheService;

    @KafkaListener(
        topics = "news-events",
        groupId = "news-consumer-group"
    )
    public void consumeNewsEvent(NewsEvent event) {
        log.info("Received Kafka event -> Topic: news-events, Type: {}, Target ID: {}", event.type(), event.newsId());
        try {
            switch (event.type()) {
                case CREATED -> handleNewsCreated(event);
                case UPDATED -> handleNewsUpdated(event);
                case DELETED -> handleNewsDeleted(event);
                case PUBLISHED -> handleNewsPublished(event);
                default -> log.warn("Failed to process Kafka event -> Target ID: {} | Reason: Unknown event type '{}'", event.newsId(), event.type());
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka event -> Type: {}, Target ID: {} | Critical error", event.type(), event.newsId(), e);
        }
    }

    private void handleNewsCreated(NewsEvent event) {
        log.info("Processing event 'CREATED' -> Target ID: {}, Title: '{}'", event.newsId(), event.title());
        newsCacheService.refreshNewsCache(event.newsId());
        log.info("Successfully processed event 'CREATED' -> Target ID: {} | Status: Cache evicted", event.newsId());
    }

    private void handleNewsUpdated(NewsEvent event) {
        log.info("Processing event 'UPDATED' -> Target ID: {}, Title: '{}'", event.newsId(), event.title());
        newsCacheService.refreshNewsCache(event.newsId());
        log.info("Successfully processed event 'UPDATED' -> Target ID: {} | Status: Cache evicted", event.newsId());
    }

    private void handleNewsDeleted(NewsEvent event) {
        log.info("Processing event 'DELETED' -> Target ID: {}", event.newsId());
        newsCacheService.refreshNewsCache(event.newsId());
        log.info("Successfully processed event 'DELETED' -> Target ID: {} | Status: Cache evicted", event.newsId());
    }

    private void handleNewsPublished(NewsEvent event) {
        log.info("Processing event 'PUBLISHED' -> Target ID: {}", event.newsId());
        newsCacheService.refreshNewsCache(event.newsId());
        log.info("Successfully processed event 'PUBLISHED' -> Target ID: {} | Status: Cache evicted", event.newsId());
    }
}
