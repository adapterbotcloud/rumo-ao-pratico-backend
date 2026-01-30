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
    private long totalQuizzes;
    private long totalQuizzesStudy;
    private long totalQuizzesEvaluation;
    private long totalQuestions;
    private long totalQuestionsRegistered;
    private long uniqueQuestionsAnswered;
    private long correctAnswers;
    private double averageScore;
    private double averageScoreStudy;
    private double averageScoreEvaluation;
    private long totalTime;
    private Map<String, Long> quizzesByTopic;
    private Map<String, Double> scoreByTopic;
    private Map<String, Long> quizzesByType;

    private long totalTopics;
}
