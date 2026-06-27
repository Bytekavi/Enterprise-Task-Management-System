package com.enterprise.tasks.api;

import com.enterprise.tasks.service.*;
import jakarta.persistence.OptimisticLockException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, Object> notFound(NotFoundException ex) {
        return error("not_found", ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Map<String, Object> forbidden(ForbiddenException ex) {
        return error("forbidden", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, Object> conflict(ConflictException ex) {
        return error("conflict", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, Object> staleWrite(OptimisticLockException ex) {
        return error("stale_write", "Task changed; refresh and retry");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> validation(MethodArgumentNotValidException ex) {
        return error("validation_failed", "Request validation failed");
    }

    private Map<String, Object> error(String code, String message) {
        return Map.of("code", code, "message", message, "timestamp", Instant.now().toString());
    }
}

