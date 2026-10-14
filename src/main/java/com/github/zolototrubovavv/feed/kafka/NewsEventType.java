package com.github.zolototrubovavv.feed.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NewsEventType {
    CREATED("news.created"),
    UPDATED("news.updated"),
    DELETED("news.deleted"),
    PUBLISHED("news.published");

    @Getter
    private String title;
}
