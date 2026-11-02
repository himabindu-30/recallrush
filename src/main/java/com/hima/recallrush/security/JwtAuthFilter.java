package com.hima.recallrush.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * FILTER = security guard at the gate.
 * Prathi request controller ki cherakamunde ee filter run avthundi.
 *
 * Pani:
 *   1. Request header lo "Authorization: Bearer <token>" undo chudadam
 *   2. Token valid aithe -> "ee user login ayyadu" ani Spring Security ki cheppadam
 *   3. Token ledu/invalid aithe -> em cheyyadu, aa tarvata SecurityConfig 401 isthundi
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter { // OncePerRequestFilter = prathi request ki okka sari matrame run

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // "Bearer " (7 chars) teesesthe token migulutundi
            try {
                Claims claims = jwtUtil.parse(token);
                Long userId = Long.valueOf(claims.getSubject());

                // Principal = userId. Controllers lo @AuthenticationPrincipal Long userId ani teeskovachu.
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                // Token fake/expired. Authentication set cheyyam -> request reject avthundi (401)
            }
        }

        chain.doFilter(request, response); // next filter / controller ki pampu
    }
}
