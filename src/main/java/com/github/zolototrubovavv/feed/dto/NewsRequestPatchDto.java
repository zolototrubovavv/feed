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
    public NewsRequestPatchDto {
        title = TrimUtils.trim(title);
        content = TrimUtils.trim(content);
    }
}
