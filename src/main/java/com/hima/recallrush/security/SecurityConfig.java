package com.hima.recallrush.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Security configuration: defines which URLs are public and which require a token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is needed for cookie/session auth. We use a JWT header, so it is disabled.
                .csrf(csrf -> csrf.disable())

                // STATELESS: the server keeps no session; every request is identified by its token
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Allow the H2 console to render inside a same-origin iframe
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))

                // Return 401 Unauthorized (instead of the default 403) when no valid token is sent
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher("/api/auth/**")).permitAll()  // register and login are public
                        .requestMatchers(antMatcher("/api/**")).authenticated()   // every other API needs a token
                        .anyRequest().permitAll())                                // static HTML/CSS/JS files are public

                // Run our JWT filter before Spring's default login filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Passwords are hashed with BCrypt; the database never stores the plain password.
    // BCrypt is one-way: the original cannot be recovered, only compared against.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
