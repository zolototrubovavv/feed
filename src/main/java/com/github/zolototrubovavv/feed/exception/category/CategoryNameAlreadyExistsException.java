package com.github.zolototrubovavv.feed.exception.category;

import com.github.zolototrubovavv.feed.exception.EntityNameAlreadyExistsException;

public class CategoryNameAlreadyExistsException extends EntityNameAlreadyExistsException {
    
    public CategoryNameAlreadyExistsException(String name) {
        super("Category", name);
    }
}
