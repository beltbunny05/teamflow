package com.teamflow.project.api;

import com.teamflow.project.exception.ProjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ProjectExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request contains invalid fields", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableMessage() {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body is invalid", Map.of());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> badRequest() {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Request contains invalid values", Map.of());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ProjectNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
                                           Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC), status.value(), code, message, fields));
    }
}
