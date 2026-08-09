package com.clearledger.user_service.service;

import com.clearledger.user_service.dto.AuthResponse;
import com.clearledger.user_service.dto.LoginRequest;
import com.clearledger.user_service.dto.RegisterRequest;
import com.clearledger.user_service.entity.RefreshToken;
import com.clearledger.user_service.entity.User;
import com.clearledger.user_service.repository.RefreshTokenRepository;
import com.clearledger.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor  // Lombok generates constructor injection for all final fields
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Check if email already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // 2. Create and save the user (password is BCrypt hashed)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        user = userRepository.save(user);

        // 3. Issue tokens
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Verify password against BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Issue tokens
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {

        // 1. Look up the token in DB
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // 2. Check it hasn't been revoked or expired
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        // 3. Revoke the old token immediately (rotation — can't be reused)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // 4. Issue fresh tokens
        return issueTokens(refreshToken.getUser());
    }

    // ── private helper ────────────────────────────────────────────────────────

    private AuthResponse issueTokens(User user) {

        // Revoke all previous refresh tokens for this user
        refreshTokenRepository.revokeAllUserTokens(user);

        // Generate new access token (JWT)
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());

        // Generate new refresh token (UUID) and persist it
        String rawRefreshToken = jwtService.generateRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(rawRefreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}