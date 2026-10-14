package com.github.zolototrubovavv.feed.exception.news;

import com.github.zolototrubovavv.feed.exception.EntityNotFoundException;

public class NewsNotFoundException extends EntityNotFoundException {

    public NewsNotFoundException(Long id) {
        super("News", id);
    }
    
}
