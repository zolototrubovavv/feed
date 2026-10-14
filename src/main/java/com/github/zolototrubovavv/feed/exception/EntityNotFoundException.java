package com.github.zolototrubovavv.feed.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityTypeName, long id) {
        super(String.format("%s with id %d not found", entityTypeName, id));
    }
}
