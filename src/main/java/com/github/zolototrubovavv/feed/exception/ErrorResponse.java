package com.github.zolototrubovavv.feed.exception;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class ErrorResponse {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
    List<FieldError> fieldErrors;

    public ErrorResponse(LocalDateTime timestamp,
                         int status,
                         String error,
                         String message,
                         String path) {
        this(timestamp, status, error, message, path, List.of());
    }

    @Value
    @Builder
    public static class FieldError {
        String field;
        String message;
    }
}