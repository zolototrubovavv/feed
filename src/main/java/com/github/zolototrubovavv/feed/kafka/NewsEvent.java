package com.github.zolototrubovavv.feed.kafka;

import java.time.LocalDateTime;

public record NewsEvent(
    NewsEventType type,
    Long newsId,
    String title,
    String content,
    Long categoryId,
    LocalDateTime timestamp
) {}
