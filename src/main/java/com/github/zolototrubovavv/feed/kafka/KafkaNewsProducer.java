package com.github.zolototrubovavv.feed.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.github.zolototrubovavv.feed.config.kafka.KafkaTopicsProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaNewsProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final KafkaTopicsProperties kafkaTopics;

    public void sendEvent(NewsEvent event) {
        kafkaTemplate.send(kafkaTopics.getNewsEvents(), String.valueOf(event.newsId()), event);
    }
}
