package com.github.zolototrubovavv.feed.dto;

import com.github.zolototrubovavv.feed.utils.TrimUtils;

import jakarta.validation.constraints.Size;

public record NewsRequestPatchDto(
    @Size(min = 3, max = 255)
    String title,

    @Size(max = 65535)
    String content,

    Long categoryId
) {
    public NewsRequestPatchDto(String title, String content, Long categoryId) {
        this.title = TrimUtils.trim(title);
        this.content = TrimUtils.trim(content);
        this.categoryId = categoryId;
    }
}
