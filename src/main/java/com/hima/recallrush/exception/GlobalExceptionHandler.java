package com.hima.recallrush.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * GLOBAL EXCEPTION HANDLER = anni controllers lo vache errors ni oka chota handle chesi
 * neat JSON {"message": "..."} ga frontend ki pampadam.
 *
 * (ResponseStatusException lanti vaatini Spring already handle chestundi; validation errors ki matrame custom.)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid fail aithe (ex: username chala chinnadi) ikkadiki vastundi
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
