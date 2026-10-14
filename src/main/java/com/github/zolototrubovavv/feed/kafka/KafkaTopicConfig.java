package com.github.zolototrubovavv.feed.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic newsTopic() {
        return TopicBuilder.name("news-events")
            .partitions(3)
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
