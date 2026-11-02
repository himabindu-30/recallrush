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
 * SECURITY CONFIG = "ye door ki lock undi, ye door open" ani rules.
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
                // CSRF: cookies/session use chesthe kavali. Memu JWT header vaadatam kabatti disable.
                .csrf(csrf -> csrf.disable())

                // STATELESS = server session store cheyyadu. Prathi request lo token tho identify.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // H2 console iframe lo open avvadaniki
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))

                // Login lekunda /api access cheste 401 Unauthorized (default 403 kaakunda)
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher("/api/auth/**")).permitAll()  // login/register everyone ki open
                        .requestMatchers(antMatcher("/api/**")).authenticated()   // migilina anni APIs ki token must
                        .anyRequest().permitAll())                                // HTML/CSS/JS files open

                // Mana JWT filter ni default login filter mundu run cheyyadam
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Password ni BCrypt tho HASH chestam. DB lo asalu password undadu.
    // BCrypt one-way: hash nundi password tirigi techukoalemu, kevalam match check cheyyagalam.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
