package com.rumoaopratico.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        LocalDateTime createdAt,
        Long totalQuestions,
        Long totalAttempts,
        Double accuracy
) {
    public UserResponse(UUID id, String name, String email, LocalDateTime createdAt) {
        this(id, name, email, createdAt, null, null, null);
    }
}
