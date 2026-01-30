package com.rumoaopratico.dto.quiz;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record QuizAnswerRequest(
        @NotNull(message = "Question ID is required")
        UUID questionId,

        @NotNull(message = "Answer is required")
        Map<String, Object> userAnswer
) {}
