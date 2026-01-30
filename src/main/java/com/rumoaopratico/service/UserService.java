package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.UpdateUserRequest;
import com.rumoaopratico.dto.response.UserResponse;
import com.rumoaopratico.dto.response.UserStatsResponse;
import com.rumoaopratico.exception.DuplicateResourceException;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.QuizAnswer;
import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            // Check if email is different and not taken
            if (!request.getEmail().equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new DuplicateResourceException("Email already in use: " + request.getEmail());
                }
                user.setEmail(request.getEmail());
            }
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(Long userId) {
        // Only count finished quizzes
        long totalAnswered = quizAttemptRepository.sumTotalQuestionsByUserId(userId);
        long totalCorrect = quizAttemptRepository.sumCorrectCountByUserId(userId);
        long totalTopics = topicRepository.count();
        long totalQuestionsRegistered = questionRepository.countAllActive();
        long uniqueQuestionsAnswered = quizAnswerRepository.countUniqueQuestionsAnsweredByUserId(userId);

        double averageScore = totalAnswered > 0 ? (double) totalCorrect / totalAnswered * 100 : 0;

        // Calculate from finished attempts only
        List<QuizAttempt> allAttempts = quizAttemptRepository.findAllByUserId(userId);
        long totalTimeSeconds = 0;
        long finishedCount = 0;
        long studyCount = 0, evalCount = 0;
        long studyCorrect = 0, studyTotal = 0;
        long evalCorrect = 0, evalTotal = 0;
        Map<String, Long> quizzesByTopic = new LinkedHashMap<>();
        Map<String, Double> scoreByTopic = new LinkedHashMap<>();
        Map<String, Long> quizzesByType = new LinkedHashMap<>();
        Map<String, long[]> topicStats = new LinkedHashMap<>();

        for (QuizAttempt attempt : allAttempts) {
            // Skip unfinished quizzes
            if (attempt.getFinishedAt() == null) continue;
            finishedCount++;

            // Total time
            if (attempt.getStartedAt() != null) {
                totalTimeSeconds += Duration.between(attempt.getStartedAt(), attempt.getFinishedAt()).getSeconds();
            }

            // Count by mode
            if (attempt.getMode() != null) {
                String modeName = attempt.getMode().name();
                quizzesByType.merge(modeName, 1L, Long::sum);
                int correct = attempt.getCorrectCount() != null ? attempt.getCorrectCount() : 0;
                int total = attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : 0;
                if ("STUDY".equals(modeName)) {
                    studyCount++;
                    studyCorrect += correct;
                    studyTotal += total;
                } else if ("EVALUATION".equals(modeName)) {
                    evalCount++;
                    evalCorrect += correct;
                    evalTotal += total;
                }
            }

            // Per-topic breakdowns from answers
            List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attempt.getId());
            Set<String> attemptTopics = new LinkedHashSet<>();
            for (QuizAnswer answer : answers) {
                String topicName = answer.getQuestion().getTopic().getName();
                attemptTopics.add(topicName);
                topicStats.computeIfAbsent(topicName, k -> new long[]{0, 0});
                long[] ts = topicStats.get(topicName);
                ts[1]++;
                if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                    ts[0]++;
                }
            }

            for (String topic : attemptTopics) {
                quizzesByTopic.merge(topic, 1L, Long::sum);
            }
        }

        // Score per topic
        for (Map.Entry<String, long[]> entry : topicStats.entrySet()) {
            long[] ts = entry.getValue();
            double score = ts[1] > 0 ? (double) ts[0] / ts[1] * 100 : 0;
            scoreByTopic.put(entry.getKey(), Math.round(score * 100.0) / 100.0);
        }

        double avgStudy = studyTotal > 0 ? (double) studyCorrect / studyTotal * 100 : 0;
        double avgEval = evalTotal > 0 ? (double) evalCorrect / evalTotal * 100 : 0;

        return UserStatsResponse.builder()
                .totalQuizzes(finishedCount)
                .totalQuizzesStudy(studyCount)
                .totalQuizzesEvaluation(evalCount)
                .totalQuestions(totalAnswered)
                .totalQuestionsRegistered(totalQuestionsRegistered)
                .uniqueQuestionsAnswered(uniqueQuestionsAnswered)
                .correctAnswers(totalCorrect)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .averageScoreStudy(Math.round(avgStudy * 100.0) / 100.0)
                .averageScoreEvaluation(Math.round(avgEval * 100.0) / 100.0)
                .totalTime(totalTimeSeconds)
                .quizzesByTopic(quizzesByTopic)
                .scoreByTopic(scoreByTopic)
                .quizzesByType(quizzesByType)
                .totalTopics(totalTopics)
                .build();
    }
}
