package com.github.zolototrubovavv.feed.exception.category;

import com.github.zolototrubovavv.feed.exception.EntityNotFoundException;

public class CategoryNotFoundException extends EntityNotFoundException {

    public CategoryNotFoundException(Long id) {
        super("Category", id);
    }
    
}
