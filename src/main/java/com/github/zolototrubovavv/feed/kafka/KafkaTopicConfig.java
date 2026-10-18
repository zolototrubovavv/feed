package com.github.zolototrubovavv.feed.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * можно вынести имя/партиции/реплики в конфиг, чтобы на разных стендах можно было задавать разные значения
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic newsTopic() {
        return TopicBuilder.name("news-events")
            .partitions(3) // тут объявляется 3 партиции, но в KafkaNewsConsumer concurrency не указан, то есть
                // будет по умолчанию только один консьюмер. Если у нас есть 3 партиции в кафке, то стоит делать такое же количество concurrency
                // Либо же, возможно тут подразумевается 3 реплики данного приложения - тогда каждый инстанс возьмет на себя по одной партиции
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic categoryTopic() {
        return TopicBuilder.name("category-events")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
