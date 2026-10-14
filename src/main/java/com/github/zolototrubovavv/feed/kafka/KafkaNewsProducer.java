package com.github.zolototrubovavv.feed.kafka;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.zolototrubovavv.feed.entity.News;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaNewsProducer {

    private static final String TOPIC = "news-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNewsCreated(News news) {
        NewsEvent event = new NewsEvent(
            NewsEventType.CREATED,
            news.getId(),
            news.getTitle(),
            news.getContent(),
            news.getCategory().getId(),
            LocalDateTime.now()
        );
        sendAsync(event);
    }

    public void sendNewsUpdated(News news) {
        NewsEvent event = new NewsEvent(
            NewsEventType.UPDATED,
            news.getId(),
            news.getTitle(),
            news.getContent(),
            news.getCategory().getId(),
            LocalDateTime.now()
        );
        sendAsync(event);
    }

    public void sendNewsDeleted(Long newsId) {
        NewsEvent event = new NewsEvent(
            NewsEventType.DELETED,
            newsId,
            null,
            null,
            null,
            LocalDateTime.now()
        );
        sendAsync(event);
    }

    public void sendNewsPublished(News news) {
        NewsEvent event = new NewsEvent(
            NewsEventType.PUBLISHED,
            news.getId(),
            news.getTitle(),
            news.getContent(),
            news.getCategory().getId(),
            LocalDateTime.now()
        );
        sendAsync(event);
    }

    private void sendAsync(NewsEvent event) {
        String key = String.valueOf(event.newsId());
        
        log.info("Sending Kafka event -> Topic: {}, Type: {}, Key: {}", TOPIC, event.type(), key);

        CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, key, event);
                log.info("Successfully sent Kafka event -> Topic: {}, Key: {} | Status: Delivered", TOPIC, key);
            } catch (Exception e) {
                log.error("Failed to send Kafka event -> Topic: {}, Type: {}, Key: {} | Critical error", TOPIC, event.type(), key, e);
            }
        });
    }
}
