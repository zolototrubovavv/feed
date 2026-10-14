package com.github.zolototrubovavv.feed.dto;

import com.github.zolototrubovavv.feed.utils.TrimUtils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewsRequestDto(
    @NotBlank
    @Size(min = 3, max = 255)
    String title,

    @NotBlank
    @Size(max = 65535)
    String content,

    @NotNull
    Long categoryId
) {
    public NewsRequestDto(String title, String content, Long categoryId) {
        this.title = TrimUtils.trim(title);
        this.content = TrimUtils.trim(content);
        this.categoryId = categoryId;
    }
}
