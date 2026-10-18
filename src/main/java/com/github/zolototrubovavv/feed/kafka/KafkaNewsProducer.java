package com.github.zolototrubovavv.feed.kafka;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.zolototrubovavv.feed.entity.News;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 1. Переменную TOPIC можно вынести в application.yml, к примеру kafka-topics.news-events: news-events
 * Создать @ConfigurationProperties и заинжектить сюда, а так же использовать и в KafkaNewsConsumer
 *
 * @Component
 * @ConfigurationProperties(prefix = "kafka-topics")
 * @Data
 * @Validated
 * public class KafkaTopicsProperties {
 *
 *     @NotBlank
 *     private String newsEvents;
 *
 * }
 *
 * 2. При создании NewsEvent можно было бы использовать тот же mapstruct, а сам маппинг из текущего класса я бы перенес в сервис,
 * тогда ответственность за маппинг перешла бы на маппер и делалась в сервисном слое, а текущий класс занимался только отправкой готового сообщения
 * и заодно обрати внимание: sendNewsCreated, sendNewsUpdated и sendNewsPublished - это один и тот же код с разным NewsEventType.
 * После переноса маппинга в маппер здесь останется один публичный метод send(NewsEvent)
 */
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

    /**
     * При отправке сообщений, можно централизованно сделать логирование через бин ProducerListener
     * (если создавать KafkaTemplate вручную, то и добавлять этот бин в KafkaTemplate надо будет вручную):
     *
     *     @Bean
     *     public ProducerListener<Object, Object> kafkaProducerListener() {
     *         return new ProducerListener<>() {
     *             @Override
     *             public void onSuccess(ProducerRecord<Object, Object> producerRecord, RecordMetadata recordMetadata) {
     *                 String topic = producerRecord.topic();
     *                 log.info(
     *                     "Outbound message to kafka topic {}: headers {}; payload {}",
     *                     topic,
     *                     decodeHeaders(producerRecord.headers()),
     *                     producerRecord.value());
     *             }
     *
     *             @Override
     *             public void onError(ProducerRecord<Object, Object> producerRecord, RecordMetadata recordMetadata, Exception exception) {
     *                 log.error(
     *                     "Error {} while outbound to kafka topic {}: headers {}; payload {}",
     *                     exception.getMessage(),
     *                     producerRecord.topic(),
     *                     decodeHeaders(producerRecord.headers()),
     *                     producerRecord.value(),
     *                     exception);
     *             }
     *         };
     *     }
     *
     *     public static Map<String, Object> decodeHeaders(Headers headers) {
     *         if (headers == null) {
     *             return Collections.emptyMap();
     *         }
     *         var decodedHeaders = new HashMap<String, Object>();
     *         headers.forEach(header -> decodedHeaders.put(header.key(), new String(header.value(), StandardCharsets.UTF_8)));
     *         return decodedHeaders;
     *     }
     */
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
