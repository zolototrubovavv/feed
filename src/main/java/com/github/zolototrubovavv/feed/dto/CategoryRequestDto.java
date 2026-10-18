package com.github.zolototrubovavv.feed.dto;

import com.github.zolototrubovavv.feed.utils.TrimUtils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
    @NotBlank
    @Size(min = 3, max = 10000)
    String name
) {
    // Можно чуть компактнее сделать конструкторы в record
    // вместо:
    //    public CategoryRequestDto(String name) {
    //        this.name = TrimUtils.trim(name);
    // записать :
    //    public CategoryRequestDto {
    //        name = TrimUtils.trim(name);
    //    }
    public CategoryRequestDto(String name) {
        this.name = TrimUtils.trim(name);
    }
}
