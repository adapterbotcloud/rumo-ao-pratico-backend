package com.rumoaopratico.dto.question;

import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        String text,
        Boolean isCorrect,
        String explanation,
        Integer orderIndex
) {}
