package com.rumoaopratico.service;

import com.rumoaopratico.dto.auth.*;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.exception.UnauthorizedException;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.UserRepository;
import com.rumoaopratico.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return new TokenResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiration());
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        log.info("User logged in: {}", user.getEmail());
        return new TokenResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiration());
    }

    public TokenResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Token is not a refresh token");
        }

        var userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        var email = jwtTokenProvider.getEmailFromToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email);

        return new TokenResponse(newAccessToken, newRefreshToken, jwtTokenProvider.getAccessTokenExpiration());
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        // Mock implementation - in production, send email
        boolean exists = userRepository.existsByEmail(request.email());
        if (exists) {
            log.info("Password recovery requested for: {}", request.email());
        }
        // Always return success to prevent email enumeration
        return Map.of("message", "If the email exists, a password recovery link has been sent.");
    }
}
