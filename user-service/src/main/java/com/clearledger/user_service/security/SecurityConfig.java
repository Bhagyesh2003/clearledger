package com.clearledger.user_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — we're using JWT, not cookies/sessions
                .csrf(AbstractHttpConfigurer::disable)

                // No sessions — each request is authenticated independently via JWT
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints are public — no token needed
                        .requestMatchers("/api/auth/**").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt automatically salts and hashes — industry standard
        return new BCryptPasswordEncoder();
    }
}

/*
User Service doesn't validate JWT itself — the Gateway already did that and forwarded X-User-Id.
So we don't add a JWT filter here. The security config just protects endpoints at the service level
as a second layer of defence.
*/
