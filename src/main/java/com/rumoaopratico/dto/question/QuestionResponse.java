package com.rumoaopratico.dto.question;

import com.rumoaopratico.model.Difficulty;
import com.rumoaopratico.model.QuestionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        UUID topicId,
        String topicName,
        QuestionType type,
        String statement,
        String explanation,
        String bibliography,
        Difficulty difficulty,
        String tags,
        Boolean isActive,
        List<QuestionOptionResponse> options,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
