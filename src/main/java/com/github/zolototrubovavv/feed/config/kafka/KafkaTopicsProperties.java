package com.github.zolototrubovavv.feed.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Component 
@ConfigurationProperties (prefix = "kafka-topics")
@Data 
@Validated 
public class KafkaTopicsProperties {

    @NotBlank 
    private String newsEvents;
    
    @Min(1) 
    private int partitions;
    
    @Min(1) 
    private int replicas;

}
