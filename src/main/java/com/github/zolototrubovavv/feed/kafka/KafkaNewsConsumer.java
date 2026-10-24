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
            topics = "${kafka-topics.news-events}"
    )
    public void consumeNewsEvent(NewsEvent event) {
        switch (event.type()) {
            case CREATED -> handleNewsCreated(event);
            case UPDATED -> handleNewsUpdated(event);
            case DELETED -> handleNewsDeleted(event);
            case PUBLISHED -> handleNewsPublished(event);
            default -> log.warn("Failed to process Kafka event -> Target ID: {} | Reason: Unknown event type '{}'", event.newsId(), event.type());
        }
    }

    private void handleNewsCreated(NewsEvent event) {
        newsCacheService.refreshNewsCache(event.newsId());
    }

    private void handleNewsUpdated(NewsEvent event) {
        newsCacheService.refreshNewsCache(event.newsId());
    }

    private void handleNewsDeleted(NewsEvent event) {
        newsCacheService.refreshNewsCache(event.newsId());
    }

    private void handleNewsPublished(NewsEvent event) {
        newsCacheService.refreshNewsCache(event.newsId());
    }
}
