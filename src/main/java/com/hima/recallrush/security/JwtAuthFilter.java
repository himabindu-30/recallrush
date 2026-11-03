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
 * Authentication filter: runs before every request reaches a controller.
 *
 * What it does:
 *   1. Looks for an "Authorization: Bearer <token>" header
 *   2. If the token is valid, tells Spring Security who the user is
 *   3. If the token is missing or invalid, does nothing; SecurityConfig then returns 401
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter { // guarantees one execution per request

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
            String token = header.substring(7); // strip the 7-character "Bearer " prefix
            try {
                Claims claims = jwtUtil.parse(token);
                Long userId = Long.valueOf(claims.getSubject());

                // The principal is the user id. Controllers read it with @AuthenticationPrincipal Long userId.
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                // Forged or expired token: leave the request unauthenticated (results in 401)
            }
        }

        chain.doFilter(request, response); // continue to the next filter or controller
    }
}
