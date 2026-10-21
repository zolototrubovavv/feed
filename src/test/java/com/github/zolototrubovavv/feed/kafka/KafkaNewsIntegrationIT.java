package com.github.zolototrubovavv.feed.kafka;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.zolototrubovavv.feed.service.NewsCacheService;

@SpringBootTest
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Integration Tests for KafkaNewsConsumer with NewsEvent")
class KafkaNewsIntegrationIT {

@Container
private static final KafkaContainer kafkaContainer = 
        new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.2")); 
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private NewsCacheService newsCacheService;

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    @DisplayName("Should successfully consume PUBLISHED NewsEvent and evict cache")
    void shouldConsumePublishedNewsEvent() {
        Long newsId = 555L;
        String topic = "news-events";
        
        NewsEvent publishedEvent = new NewsEvent(
            NewsEventType.PUBLISHED,
            newsId,
            "Integration Test Title",
            "Some test content for news",
            1L,
            java.time.LocalDateTime.now()
        );

        kafkaTemplate.send(topic, newsId.toString(), publishedEvent);

        await().atMost(5, SECONDS).untilAsserted(() -> {
            Mockito.verify(newsCacheService, Mockito.atLeastOnce()).refreshNewsCache(newsId);
        });
    }

    @Test
    @DisplayName("Should successfully consume DELETED NewsEvent")
    void shouldConsumeDeletedNewsEvent() {
        Long newsId = 777L;
        String topic = "news-events";
        NewsEvent deletedEvent = new NewsEvent(
            NewsEventType.DELETED,
            newsId,
            null,
            null,
            null,
            java.time.LocalDateTime.now()
        );

        kafkaTemplate.send(topic, newsId.toString(), deletedEvent);

        await().atMost(5, SECONDS).untilAsserted(() -> {
            Mockito.verify(newsCacheService, Mockito.atLeastOnce()).refreshNewsCache(newsId);
        });
    }
}
