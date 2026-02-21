package com.smartprocure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Data Integrity Violation");
        // Check if it's a unique constraint violation (simplified check)
        if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
            error.put("message", "A record with this identifier already exists (e.g., email or unique field).");
        } else {
            error.put("message", "Database constraint violation.");
        }
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            org.springframework.security.authorization.AuthorizationDeniedException.class,
            org.springframework.security.access.AccessDeniedException.class
    })
    public ResponseEntity<Map<String, String>> handleAccessDenied(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", "Access Denied");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequestExceptions(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage()); // Expose error message for debugging
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
