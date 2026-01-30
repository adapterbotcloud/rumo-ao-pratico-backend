package com.rumoaopratico.dto.stats;

import java.util.List;
import java.util.Map;

public record DashboardStatsResponse(
        long totalQuestions,
        long totalAttempts,
        long totalAnswers,
        double overallAccuracy,
        List<TopicStat> byTopic,
        Map<String, Long> byType,
        Map<String, Long> byDifficulty,
        List<RecentAttempt> recentAttempts
) {
    public record TopicStat(
            String topicId,
            String topicName,
            long questionCount,
            long answeredCount,
            double accuracy
    ) {}

    public record RecentAttempt(
            String attemptId,
            String mode,
            int totalQuestions,
            int correctCount,
            String startedAt,
            String finishedAt
    ) {}
}
