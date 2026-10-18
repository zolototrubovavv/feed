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

    /**
     * KafkaNewsConfig объявляет свои ConsumerFactory/ProducerFactory/KafkaTemplate, поэтому KafkaAutoConfiguration выключается
     * и блок spring.kafka.* из application.yml не применяется вообще (включая group-id, auto-offset-reset и bootstrap-servers).
     * groupId здесь сейчас - единственное работающее значение, убирать его нельзя.
     *
     * Я бы убрал кастомные фабрики и перенёс всё в application.yml (spring.kafka.consumer.*, spring.kafka.producer.*),
     * после чего можно было бы убрать groupId из аннотации, так как оно бы тянулось из application.yml spring.kafka.consumer.
     *
     * Имя топика (topics) уже есть в переменной TOPIC в KafkaNewsProducer.java - можно её вынести в application.yml
     * и переиспользовать тут через SpEL: topics = "${kafka-topics.news-event}" и в KafkaNewsProducer.java через @ConfigurationProperties
     */
    @KafkaListener(
            topics = "news-events",
            groupId = "news-consumer-group"
    )
    public void consumeNewsEvent(NewsEvent event) {
        // Если потребитель читает по одному сообщению, то данный лог можно реализовать через RecordInterceptor и объявить в конфиге кафки:
        //    @Bean
        //    public RecordInterceptor<Object, Object> kafkaConsumerListener() {
        //        return (consumerRecord, consumer) -> {
        //            log.info(
        //                "Inbound message from kafka\ntopic: {}\nheaders: {}\npayload: {}",
        //                consumerRecord.topic(),
        //                decodeHeaders(consumerRecord.headers()),
        //                consumerRecord.value());
        //            return consumerRecord;
        //        };
        //    }
        //
        //     public static Map<String, Object> decodeHeaders(Headers headers) {
        //        if (headers == null) {
        //            return Collections.emptyMap();
        //        }
        //        var decodedHeaders = new HashMap<String, Object>();
        //        headers.forEach(header -> decodedHeaders.put(header.key(), new String(header.value(), StandardCharsets.UTF_8)));
        //        return decodedHeaders;
        //    }

        // Но он добавляется в ConcurrentKafkaListenerContainerFactory (factory.setRecordInterceptor(...)) если фабрика создаётся вручную.
        // Если же перейдешь на использование автоконфига из application.yml, как описано в KafkaNewsConfig, то будет достаточно просто объявить бин, как описано выше
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
        // логи это хорошо, но кажется их много и будут спамить в ELK
        // Тут лог пишется о том, что процесс стартанул, после него считай ни строчки бизнес кода и сразу следующий лог
        // внутри newsCacheService.refreshNewsCache(event.newsId()) :
        // log.info("Executing entity cache eviction -> Target ID: {}", id);
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
