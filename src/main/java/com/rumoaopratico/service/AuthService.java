package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.LoginRequest;
import com.rumoaopratico.dto.request.RefreshTokenRequest;
import com.rumoaopratico.dto.request.RegisterRequest;
import com.rumoaopratico.dto.response.AuthResponse;
import com.rumoaopratico.dto.response.UserResponse;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.exception.DuplicateResourceException;
import com.rumoaopratico.exception.UnauthorizedException;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.UserRepository;
import com.rumoaopratico.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return generateAuthResponse(user);
    }

    public void forgotPassword(String email) {
        // In production, this would send a password reset email
        // For now, just verify the email exists
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with this email"));
        log.info("Password reset requested for: {}", email);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessExpiration(),
                UserResponse.from(user)
        );
    }
}
