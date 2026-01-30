package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.model.enums.QuizMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntryResponse {
    private Long attemptId;
    private QuizMode mode;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double score;
    private Long totalTimeSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private List<String> topics;

    public static HistoryEntryResponse from(QuizAttempt attempt, List<String> topicNames) {
        int correct = attempt.getCorrectCount() != null ? attempt.getCorrectCount() : 0;
        int total = attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : 0;
        double score = total > 0 ? (double) correct / total * 100.0 : 0.0;

        long timeSeconds = 0;
        if (attempt.getStartedAt() != null && attempt.getFinishedAt() != null) {
            timeSeconds = Duration.between(attempt.getStartedAt(), attempt.getFinishedAt()).getSeconds();
        }

        return HistoryEntryResponse.builder()
                .attemptId(attempt.getId())
                .mode(attempt.getMode())
                .totalQuestions(total)
                .correctAnswers(correct)
                .score(Math.round(score * 100.0) / 100.0)
                .totalTimeSeconds(timeSeconds)
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .topics(topicNames != null ? topicNames : List.of())
                .build();
    }
}
