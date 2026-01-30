package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.model.enums.QuizMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptResponse {
    private Long id;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalQuestions;
    private Integer correctCount;
    private QuizMode mode;
    private Map<String, Object> config;
    private List<QuestionResponse> questions;

    public static QuizAttemptResponse from(QuizAttempt attempt) {
        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .mode(attempt.getMode())
                .config(attempt.getConfigJson())
                .build();
    }

    public static QuizAttemptResponse fromWithQuestions(QuizAttempt attempt, List<QuestionResponse> questions) {
        QuizAttemptResponse response = from(attempt);
        response.setQuestions(questions);
        return response;
    }
}
