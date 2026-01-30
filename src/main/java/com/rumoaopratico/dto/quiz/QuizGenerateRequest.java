package com.rumoaopratico.dto.quiz;

import com.rumoaopratico.model.Difficulty;
import com.rumoaopratico.model.QuestionType;
import com.rumoaopratico.model.QuizMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record QuizGenerateRequest(
        List<UUID> topicIds,

        @NotNull(message = "Number of questions is required")
        @Min(value = 1, message = "At least 1 question is required")
        @Max(value = 100, message = "Maximum 100 questions per quiz")
        Integer count,

        List<QuestionType> types,

        Difficulty difficulty,

        @NotNull(message = "Quiz mode is required")
        QuizMode mode
) {}
