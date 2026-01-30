package com.rumoaopratico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    // Fields matching frontend UserStats interface
    private long totalQuizzes;
    private long totalQuestions;
    private long correctAnswers;
    private double averageScore;
    private long totalTime;
    private Map<String, Long> quizzesByTopic;
    private Map<String, Double> scoreByTopic;
    private Map<String, Long> quizzesByType;

    // Extra fields
    private long totalTopics;
}
