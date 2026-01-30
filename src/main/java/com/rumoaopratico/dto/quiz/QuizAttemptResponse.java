package com.rumoaopratico.dto.quiz;

import com.rumoaopratico.dto.question.QuestionResponse;
import com.rumoaopratico.model.QuizMode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuizAttemptResponse(
        UUID id,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Integer totalQuestions,
        Integer correctCount,
        QuizMode mode,
        Map<String, Object> configJson,
        List<QuizAnswerResponse> answers,
        List<QuestionResponse> questions,
        LocalDateTime createdAt
) {}
