package com.github.zolototrubovavv.feed.exception.category;

import java.util.List;

public class CategoryNotEmptyException extends RuntimeException {
    public CategoryNotEmptyException(String name, List<Long> newsIdList) {
        super(String.format(
            "Category %s has news with ids=[%s]",
            name,
            newsIdList.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(", "))));
    }
}
