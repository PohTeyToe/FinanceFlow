package com.financeflow.gateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.financeflow.gateway.config.JwtConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtConfig jwtConfig;
    private String testSecret;

    @BeforeEach
    void setUp() {
        // Generate a valid base64-encoded secret (at least 256 bits / 32 bytes)
        testSecret = Base64.getEncoder().encodeToString(
                "this-is-a-test-secret-key-that-is-long-enough-for-hs256".getBytes());

        jwtConfig = new JwtConfig();
        jwtConfig.setSecret(testSecret);

        jwtService = new JwtService(jwtConfig);
    }

    @Test
    void shouldValidateCorrectToken() {
        String token = createTestToken("user123", "test@example.com", 3600000); // 1 hour

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = createTestToken("user123", "test@example.com", -1000); // Already expired

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }

    @Test
    void shouldExtractUserId() {
        String token = createTestToken("user123", "test@example.com", 3600000);

        String userId = jwtService.extractUserId(token);

        assertEquals("user123", userId);
    }

    @Test
    void shouldExtractEmail() {
        String token = createTestToken("user123", "test@example.com", 3600000);

        String email = jwtService.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void shouldDetectExpiredToken() {
        String token = createTestToken("user123", "test@example.com", -1000);

        assertTrue(jwtService.isTokenExpired(token));
    }

    @Test
    void shouldDetectNonExpiredToken() {
        String token = createTestToken("user123", "test@example.com", 3600000);

        assertFalse(jwtService.isTokenExpired(token));
    }

    private String createTestToken(String userId, String email, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }
}
