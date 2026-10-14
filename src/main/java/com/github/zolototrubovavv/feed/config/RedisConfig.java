package com.github.zolototrubovavv.feed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Initializing Redis components -> Action: Configuring RedisTemplate bean");
        try {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);

            PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType("com.github.zolototrubovavv") 
                    .allowIfBaseType(java.util.Collection.class)
                    .build();
            
            GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                    .enableDefaultTyping(typeValidator)
                    .build();
            
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(jsonSerializer);
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(jsonSerializer);
            
            log.info("Successfully initialized Redis components -> Type: RedisTemplate<String, Object> | Status: Bean registered");
            return template;
        } catch (Exception e) {
            log.error("Failed to initialize Redis components -> Action: Bean registration | Critical error", e);
            throw e;
        }
    }
}
