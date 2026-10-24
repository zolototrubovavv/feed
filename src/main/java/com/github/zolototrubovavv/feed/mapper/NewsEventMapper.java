package com.github.zolototrubovavv.feed.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.github.zolototrubovavv.feed.config.MapStructConfig;
import com.github.zolototrubovavv.feed.entity.News;
import com.github.zolototrubovavv.feed.kafka.NewsEvent;
import com.github.zolototrubovavv.feed.kafka.NewsEventType;

@Mapper(config = MapStructConfig.class, imports = LocalDateTime.class)
public interface NewsEventMapper {

    @Mapping(target = "type", source = "eventType")
    @Mapping(target = "newsId", source = "news.id")
    @Mapping(target = "title", source = "news.title")
    @Mapping(target = "content", source = "news.content")
    @Mapping(target = "categoryId", source = "news.category.id")
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.now())")
    NewsEvent toEvent(News news, NewsEventType eventType);
    
    
    @Mapping(target = "type", source = "eventType")
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.now())")
    NewsEvent toEvent(Long newsId, NewsEventType eventType);
}
