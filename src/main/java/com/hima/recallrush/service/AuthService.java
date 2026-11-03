package com.hima.recallrush.service;

import com.hima.recallrush.dto.Dtos;
import com.hima.recallrush.model.User;
import com.hima.recallrush.repository.UserRepository;
import com.hima.recallrush.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Authentication service: registration and login logic.
 */
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final DeckService deckService;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder,
                       JwtUtil jwtUtil, DeckService deckService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.deckService = deckService;
    }

    public Dtos.AuthResponse register(Dtos.AuthRequest req) {
        String username = req.username().trim().toLowerCase();

        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken"); // 409
        }

        // Store only the password hash, never the plain password
        User user = userRepo.save(new User(username, encoder.encode(req.password())));

        deckService.createStarterDeck(user); // welcome deck with sample cards

        // Log the user in immediately after registration by issuing a token
        return new Dtos.AuthResponse(jwtUtil.generateToken(user.getId(), user.getUsername()), user.getUsername());
    }

    public Dtos.AuthResponse login(Dtos.AuthRequest req) {
        String username = req.username().trim().toLowerCase();

        // Unknown user and wrong password return the SAME message, so an attacker
        // cannot tell which usernames exist.
        User user = userRepo.findByUsername(username)
                .filter(u -> encoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        return new Dtos.AuthResponse(jwtUtil.generateToken(user.getId(), user.getUsername()), user.getUsername());
    }
}
