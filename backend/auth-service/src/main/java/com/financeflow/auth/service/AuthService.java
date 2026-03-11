package com.financeflow.auth.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeflow.auth.dto.AuthResponse;
import com.financeflow.auth.dto.LoginRequest;
import com.financeflow.auth.dto.RefreshTokenRequest;
import com.financeflow.auth.dto.RegisterRequest;
import com.financeflow.auth.dto.UserDto;
import com.financeflow.auth.exception.AuthException;
import com.financeflow.auth.exception.UserAlreadyExistsException;
import com.financeflow.auth.model.RefreshToken;
import com.financeflow.auth.model.User;
import com.financeflow.auth.repository.RefreshTokenRepository;
import com.financeflow.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .isActive(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (!storedToken.isValid()) {
            throw new AuthException("Refresh token is expired or revoked", HttpStatus.UNAUTHORIZED);
        }

        // Verify the JWT is valid
        try {
            String email = jwtService.extractEmail(request.getRefreshToken());
            if (!email.equals(storedToken.getUser().getEmail())) {
                throw new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        User user = storedToken.getUser();

        // Revoke the old refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(User user) {
        refreshTokenRepository.revokeAllUserTokens(user.getId());
    }

    public UserDto getCurrentUser(User user) {
        return UserDto.fromUser(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        // Revoke any existing refresh tokens for this user
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token in database
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000) // Convert to seconds
                .user(UserDto.fromUser(user))
                .build();
    }
}
