package com.hima.recallrush.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler: catches errors thrown from any controller in one place
 * and returns them to the frontend as a tidy JSON body {"message": "..."}.
 *
 * (Spring already handles ResponseStatusException; this class adds validation errors.)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Invoked when @Valid fails (for example, a username that is too short)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Invalid input");
        return Map.of("message", message);
    }
}
