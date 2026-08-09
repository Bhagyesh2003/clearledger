package com.clearledger.user_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;   // 900000ms = 15 min

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;  // 604800000ms = 7 days

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate a signed JWT access token with userId as the subject
    public String generateAccessToken(String userId, String email) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh token is just a random UUID — not a JWT
    // We store it in DB and look it up on each refresh request
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // Validate token signature and expiry, return claims if valid
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return validateToken(token).getSubject();
    }
}

/*
The refresh token is a plain UUID stored in the database — not a JWT.
This is intentional. A JWT refresh token can't be revoked (it's stateless).
A DB-stored UUID can be deleted or flagged revoked instantly. For a financial app, revocability matters.
*/
