package com.financeflow.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import com.financeflow.auth.config.JwtConfig;
import com.financeflow.auth.model.User;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        when(jwtConfig.getSecret()).thenReturn("dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3Rpbmctb25seS1kb25vdC11c2UtaW4tcHJvZHVjdGlvbg==");
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600000L);
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(604800000L);

        jwtService = new JwtService(jwtConfig);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should generate valid access token")
    void shouldGenerateValidAccessToken() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.extractEmail(token)).isEqualTo(testUser.getEmail());
        assertThat(jwtService.extractUserId(token)).isEqualTo(testUser.getId().toString());
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void shouldGenerateValidRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.extractEmail(token)).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Should validate token correctly")
    void shouldValidateTokenCorrectly() {
        String token = jwtService.generateAccessToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Create a service with very short expiration
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(-1000L); // Already expired
        JwtService shortLivedService = new JwtService(jwtConfig);
        
        String token = shortLivedService.generateAccessToken(testUser);

        assertThat(shortLivedService.isTokenExpired(token)).isTrue();
    }

    @Test
    @DisplayName("Should reject token with wrong user")
    void shouldRejectTokenWithWrongUser() {
        String token = jwtService.generateAccessToken(testUser);

        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .email("other@example.com")
                .passwordHash("hashedPassword")
                .firstName("Other")
                .lastName("User")
                .isActive(true)
                .build();

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateAccessToken(testUser);

        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        String token = jwtService.generateAccessToken(testUser);

        String userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo(testUser.getId().toString());
    }
}
