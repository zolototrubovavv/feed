package com.github.zolototrubovavv.feed.dto;

import java.time.LocalDateTime;

import com.github.zolototrubovavv.feed.enums.NewsStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDto {

    private Long id;
    private String title;
    private String content;
    private NewsStatus status;
    private LocalDateTime publishedAt;
    private CategoryDto category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
