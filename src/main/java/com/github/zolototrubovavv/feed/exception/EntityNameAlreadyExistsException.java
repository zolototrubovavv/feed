package com.github.zolototrubovavv.feed.exception;

public class EntityNameAlreadyExistsException extends RuntimeException {
    public EntityNameAlreadyExistsException(String entityTypeName, String name) {
        super(String.format("%s with name %s already exists", entityTypeName, name));
    }
}
