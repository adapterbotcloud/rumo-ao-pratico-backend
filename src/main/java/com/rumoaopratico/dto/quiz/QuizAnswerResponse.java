package com.rumoaopratico.dto.quiz;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record QuizAnswerResponse(
        UUID id,
        UUID questionId,
        Map<String, Object> userAnswerJson,
        Boolean isCorrect,
        LocalDateTime answeredAt
) {}
