package com.hima.recallrush.controller;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER = API doors. Analogy: Restaurant lo Waiter.
 * Customer (browser) request istadu -> waiter chef (service) ki cheptadu -> result techi istadu.
 *
 * HTTP methods:  GET = read,  POST = create,  PUT = update,  DELETE = delete
 */
@RestController                      // return chesina objects ni automatic ga JSON ga marchutundi
@RequestMapping("/api/auth")         // ee controller URLs anni /api/auth tho start
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/register  body: {"username": "...", "password": "..."}
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // success ayithe 201
    public Dtos.AuthResponse register(@Valid @RequestBody Dtos.AuthRequest req) {
        // @RequestBody = JSON ni Java object ga marchi istundi
        // @Valid       = DTO lo unna @NotBlank, @Size checks run chestundi
        return authService.register(req);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public Dtos.AuthResponse login(@Valid @RequestBody Dtos.AuthRequest req) {
        return authService.login(req);
    }
}
