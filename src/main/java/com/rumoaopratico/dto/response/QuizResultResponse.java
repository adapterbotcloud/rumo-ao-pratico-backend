package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.enums.QuizMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {
    private Long attemptId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double score;
    private Long totalTimeSeconds;
    private QuizMode mode;
    private List<QuizResultQuestionResponse> questions;
    private Map<String, BreakdownEntry> breakdownByTopic;
    private Map<String, BreakdownEntry> breakdownByType;

    // Keep old fields for backward compat
    private Integer correctCount;
    private Double successRate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownEntry {
        private int total;
        private int correct;
        private double score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizResultQuestionResponse {
        private int index;
        private Long questionId;
        private QuestionResponse question;
        private String userAnswer;
        private boolean correct;
    }
}
