package com.github.zolototrubovavv.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

// В целом, место для объявления данных аннотаций - нормальное. Можно пойти чуть дальше и под каждую создать свой
// java конфиг

// @EnableKafka можно убрать: Spring Boot включает её сам (KafkaAnnotationDrivenConfiguration), пока в контексте нет своего KafkaListenerAnnotationBeanPostProcessor.
// Если хочется оставить явно, то лучшее место ей в KafkaNewsConfig

@EnableKafka
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class FeedApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedApplication.class, args);
	}

    // В классе, который занимается запуском приложения, лучше не добавлять бинов. Если что-то нужно, то можно создать
    // класс конфигов, к примеру ApplicationConfig, где и объявить эти бины. Общее правило: класс с @SpringBootApplication только точка входа, а бины в @Configuration-классах

    // Но тут есть другая проблема
    // Текущий бин не просто лишний - он отключает автоконфигурацию ValidationAutoConfiguration (там @ConditionalOnMissingBean)
    // и подменяет её менее настроенной версией (без MessageSource для локализованных сообщений валидации)
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

}
