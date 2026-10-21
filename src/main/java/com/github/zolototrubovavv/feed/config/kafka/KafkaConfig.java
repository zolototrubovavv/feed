package com.github.zolototrubovavv.feed.config.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.ProducerListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Configuration 
public class KafkaConfig {
    @Bean 
    public RecordInterceptor<Object, Object> kafkaConsumerListener() {
        return (consumerRecord, consumer) -> {
            log.info(
                "Received Kafka event -> Topic: {}; headers: {}; payload: {}",
                consumerRecord.topic(),
                decodeHeaders(consumerRecord.headers()),
                consumerRecord.value());
            return consumerRecord;
        };
    }

    @Bean
    public ProducerListener<Object, Object> kafkaProducerListener() {
        return new ProducerListener<>() {
            @Override
            public void onSuccess(ProducerRecord<Object, Object> producerRecord, RecordMetadata recordMetadata) {
                String topic = producerRecord.topic();
                log.info(
                    "Send Kafka event -> Topic: {}; headers {}; payload {}",
                    topic,
                    decodeHeaders(producerRecord.headers()),
                    producerRecord.value());
            }

            @Override
            public void onError(ProducerRecord<Object, Object> producerRecord, RecordMetadata recordMetadata, Exception exception) {
                log.error(
                    "Failed to send Kafka event -> Error: {}; topic {}; headers {}; payload {}",
                    exception.getMessage(),
                    producerRecord.topic(),
                    decodeHeaders(producerRecord.headers()),
                    producerRecord.value(),
                    exception);
            }
        };
    }

    public static Map<String, Object> decodeHeaders(Headers headers) {
        if (headers == null) {
            return Collections.emptyMap();
        }
        var decodedHeaders = new HashMap<String, Object>();
        headers.forEach(header -> decodedHeaders.put(header.key(), new String(header.value(), StandardCharsets.UTF_8)));
        return decodedHeaders;
    }
}
