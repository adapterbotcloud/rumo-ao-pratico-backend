package com.rumoaopratico.service;

import com.rumoaopratico.dto.auth.LoginRequest;
import com.rumoaopratico.dto.auth.RegisterRequest;
import com.rumoaopratico.dto.auth.TokenResponse;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.UserRepository;
import com.rumoaopratico.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(any(UUID.class), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class), anyString())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

        TokenResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateAccessToken(userId, "test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_shouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_shouldReturnTokensOnValidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("test@example.com", "password123"));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userId, "test@example.com")).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

        TokenResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should return message for forgot password regardless of email existence")
    void forgotPassword_shouldReturnGenericMessage() {
        var request = new com.rumoaopratico.dto.auth.ForgotPasswordRequest("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        var response = authService.forgotPassword(request);

        assertThat(response).containsKey("message");
        assertThat(response.get("message")).contains("password recovery link");
    }
}
