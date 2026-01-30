package com.rumoaopratico.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public TokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
