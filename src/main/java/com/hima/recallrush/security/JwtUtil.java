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
 * JWT = JSON Web Token = user ki ichhe "digital pass / wristband".
 *
 * Analogy: Concert lo entry ayyaka wristband istaru. Tarvata prathi sari ID chupinchakkarledu,
 * wristband chupisthe chaalu. JWT kuda alaane: login ayyaka token istam, tarvata prathi request lo
 * aa token pampali.
 *
 * Token lo 3 parts untayi:  HEADER . PAYLOAD (userId, expiry) . SIGNATURE (secret tho sign)
 * Evaraina payload marchaalani chuste signature match avvadu -> reject.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    // @Value = application.properties nundi values techukuntundi
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Secret text ni cryptographic key ga marchadam (HMAC-SHA256)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Login success aithe token create cheyyadam
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))   // token evari di: user id
                .claim("username", username)        // extra info
                .issuedAt(now)                       // eppudu create ayyindi
                .expiration(new Date(now.getTime() + expirationMs)) // eppudu expire avthundi
                .signWith(key)                       // secret tho sign
                .compact();                          // final string ga marchu
    }

    // Token ni verify chesi andulo data techukuntundi. Invalid/expired aithe exception vastundi.
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)       // signature check
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
