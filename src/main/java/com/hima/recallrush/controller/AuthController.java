package com.hima.recallrush.controller;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller: exposes the REST API endpoints.
 * It receives the HTTP request, delegates the work to the service, and returns the result.
 *
 * HTTP methods:  GET = read,  POST = create,  PUT = update,  DELETE = delete
 */
@RestController                      // converts returned objects to JSON automatically
@RequestMapping("/api/auth")         // every URL in this controller starts with /api/auth
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/register   body: {"username": "...", "password": "..."}
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // 201 on success
    public Dtos.AuthResponse register(@Valid @RequestBody Dtos.AuthRequest req) {
        // @RequestBody converts the JSON body into a Java object
        // @Valid runs the validation annotations (@NotBlank, @Size) declared on the DTO
        return authService.register(req);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public Dtos.AuthResponse login(@Valid @RequestBody Dtos.AuthRequest req) {
        return authService.login(req);
    }
}
