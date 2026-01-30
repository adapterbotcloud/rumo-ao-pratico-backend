package com.rumoaopratico.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionOptionRequest(
        @NotBlank(message = "Option text is required")
        String text,

        @NotNull(message = "isCorrect is required")
        Boolean isCorrect,

        String explanation,

        Integer orderIndex
) {}
