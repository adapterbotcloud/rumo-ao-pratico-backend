package com.rumoaopratico.service;

import com.rumoaopratico.dto.stats.DashboardStatsResponse;
import com.rumoaopratico.model.Difficulty;
import com.rumoaopratico.model.QuestionType;
import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.model.Topic;
import com.rumoaopratico.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatsService {

    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final TopicRepository topicRepository;

    public StatsService(QuestionRepository questionRepository, QuizAttemptRepository quizAttemptRepository,
                        QuizAnswerRepository quizAnswerRepository, TopicRepository topicRepository) {
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.topicRepository = topicRepository;
    }

    public DashboardStatsResponse getDashboardStats(UUID userId) {
        long totalQuestions = questionRepository.countByUserIdAndIsActiveTrue(userId);
        long totalAttempts = quizAttemptRepository.countByUserId(userId);
        long totalAnswers = quizAnswerRepository.countByUserId(userId);
        long correctAnswers = quizAnswerRepository.countCorrectByUserId(userId);
        double overallAccuracy = totalAnswers > 0 ? (double) correctAnswers / totalAnswers * 100.0 : 0.0;

        // Stats by topic
        List<Topic> topics = topicRepository.findByUserId(userId);
        List<DashboardStatsResponse.TopicStat> topicStats = new ArrayList<>();
        for (Topic topic : topics) {
            long qCount = topicRepository.countActiveQuestionsByTopicId(topic.getId());
            long answered = quizAnswerRepository.countByUserIdAndTopicId(userId, topic.getId());
            long correct = quizAnswerRepository.countCorrectByUserIdAndTopicId(userId, topic.getId());
            double topicAccuracy = answered > 0 ? (double) correct / answered * 100.0 : 0.0;
            topicStats.add(new DashboardStatsResponse.TopicStat(
                    topic.getId().toString(),
                    topic.getName(),
                    qCount,
                    answered,
                    Math.round(topicAccuracy * 100.0) / 100.0
            ));
        }

        // Stats by type
        Map<String, Long> byType = new LinkedHashMap<>();
        for (QuestionType type : QuestionType.values()) {
            byType.put(type.name(), questionRepository.countByUserIdAndType(userId, type));
        }

        // Stats by difficulty
        Map<String, Long> byDifficulty = new LinkedHashMap<>();
        for (Difficulty diff : Difficulty.values()) {
            byDifficulty.put(diff.name(), questionRepository.countByUserIdAndDifficulty(userId, diff));
        }

        // Recent attempts
        var recentPage = quizAttemptRepository.findByUserIdWithFilters(
                userId, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        List<DashboardStatsResponse.RecentAttempt> recentAttempts = recentPage.getContent().stream()
                .map(a -> new DashboardStatsResponse.RecentAttempt(
                        a.getId().toString(),
                        a.getMode().name(),
                        a.getTotalQuestions(),
                        a.getCorrectCount(),
                        a.getStartedAt() != null ? a.getStartedAt().toString() : null,
                        a.getFinishedAt() != null ? a.getFinishedAt().toString() : null
                ))
                .toList();

        return new DashboardStatsResponse(
                totalQuestions,
                totalAttempts,
                totalAnswers,
                Math.round(overallAccuracy * 100.0) / 100.0,
                topicStats,
                byType,
                byDifficulty,
                recentAttempts
        );
    }
}
