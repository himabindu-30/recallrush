package com.hima.recallrush.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Creates and verifies JWTs (JSON Web Tokens).
 *
 * After a successful login the server issues a signed token. The client sends it
 * with every later request, so the user does not have to log in again each time.
 *
 * A token has three parts:  HEADER . PAYLOAD (user id, expiry) . SIGNATURE
 * If anyone alters the payload, the signature no longer matches and the token is rejected.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    // @Value injects values from application.properties
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Convert the secret text into an HMAC-SHA256 signing key
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Creates a token after a successful login
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))   // whom the token belongs to: the user id
                .claim("username", username)        // extra information
                .issuedAt(now)                       // when it was created
                .expiration(new Date(now.getTime() + expirationMs)) // when it expires
                .signWith(key)                       // sign with the secret key
                .compact();                          // build the final string
    }

    // Verifies the signature and expiry and returns the payload.
    // Throws an exception if the token is invalid or expired.
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)       // check the signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
