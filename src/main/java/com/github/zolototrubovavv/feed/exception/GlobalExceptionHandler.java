package com.github.zolototrubovavv.feed.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        String requiredTypeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String message = String.format(
            "Parameter '%s' with value '%s' cannot be converted to type '%s'",
            ex.getName(), ex.getValue(), requiredTypeName
        );

        log.warn("REST API Exception -> Path: {}, Method: {} | Type Mismatch Validation Failed", 
            request.getRequestURI(), request.getMethod());

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(message)
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        log.warn("REST API Exception -> Path: {}, Method: {} | Invalid Argument Provided: {}", 
            request.getRequestURI(), request.getMethod(), ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
        RuntimeException ex,
        HttpServletRequest request
    ) {
        log.error("Internal Server Exception -> Path: {}, Method: {} | Runtime Error Occurred", 
            request.getRequestURI(), request.getMethod(), ex);

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("An internal server error occurred")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(f -> new ErrorResponse.FieldError(
                f.getField(),
                f.getDefaultMessage()
            ))
            .toList();

        log.warn("REST API Exception -> Path: {}, Method: {} | Validation Failed: {} fields invalid", 
            request.getRequestURI(), request.getMethod(), fieldErrors.size());

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("One or more fields are invalid")
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
        ResponseStatusException ex,
        HttpServletRequest request
    ) {
        var statusCode = ex.getStatusCode();
        HttpStatus resolvedStatus = HttpStatus.resolve(statusCode.value());
        String errorReason = resolvedStatus != null ? resolvedStatus.getReasonPhrase() : "HTTP Error Status";

        if (statusCode.is5xxServerError()) {
            log.error("Response Status Exception -> Path: {}, Method: {}, Status: {} | Server Error", 
                request.getRequestURI(), request.getMethod(), statusCode.value(), ex);
        } else {
            log.warn("Response Status Exception -> Path: {}, Method: {}, Status: {} | Client Error: {}", 
                request.getRequestURI(), request.getMethod(), statusCode.value(), ex.getMessage());
        }

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(statusCode.value())
            .error(errorReason)
            .message(ex.getReason() != null ? ex.getReason() : ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(statusCode).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(
        EntityNotFoundException ex,
        HttpServletRequest request
    ) {
        log.warn("REST API Exception -> Path: {}, Method: {} | Resource Not Found: {}", 
            request.getRequestURI(), request.getMethod(), ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(EntityNameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNameAlreadyExists(
        EntityNameAlreadyExistsException ex,
        HttpServletRequest request
    ) {
        log.warn("REST API Exception -> Path: {}, Method: {} | Conflict: Data uniqueness violation -> {}", 
            request.getRequestURI(), request.getMethod(), ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error("Critical Unhandled Exception -> Path: {}, Method: {} | Unexpected error occurred", 
            request.getRequestURI(), request.getMethod(), ex);

        ErrorResponse body = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Unexpected error occurred")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}