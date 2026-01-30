package com.rumoaopratico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private long totalQuestions;
    private long totalQuizAttempts;
    private long totalAnswered;
    private long totalCorrect;
    private double successRate;
    private long totalTopics;
}
