package com.github.zolototrubovavv.feed.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor 
public class KafkaTopicConfig {

    private final KafkaTopicsProperties kafkaTopics;

    @Bean
    public NewTopic newsTopic() {
        return TopicBuilder.name(kafkaTopics.getNewsEvents())
            .partitions(kafkaTopics.getPartitions())
            .replicas(kafkaTopics.getReplicas())
            .build();
    }
}
