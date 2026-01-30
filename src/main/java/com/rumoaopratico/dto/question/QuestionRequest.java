package com.rumoaopratico.dto.question;

import com.rumoaopratico.model.Difficulty;
import com.rumoaopratico.model.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record QuestionRequest(
        @NotNull(message = "Topic ID is required")
        UUID topicId,

        @NotNull(message = "Question type is required")
        QuestionType type,

        @NotBlank(message = "Statement is required")
        String statement,

        String explanation,

        String bibliography,

        @NotNull(message = "Difficulty is required")
        Difficulty difficulty,

        String tags,

        @Valid
        List<QuestionOptionRequest> options
) {}
