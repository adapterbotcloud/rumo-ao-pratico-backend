package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.UpdateUserRequest;
import com.rumoaopratico.dto.response.UserResponse;
import com.rumoaopratico.dto.response.UserStatsResponse;
import com.rumoaopratico.exception.DuplicateResourceException;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
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

    public UserStatsResponse getUserStats(Long userId) {
        long totalQuestions = questionRepository.countByUserIdAndIsActiveTrue(userId);
        long totalAttempts = quizAttemptRepository.countByUserId(userId);
        long totalAnswered = quizAttemptRepository.sumTotalQuestionsByUserId(userId);
        long totalCorrect = quizAttemptRepository.sumCorrectCountByUserId(userId);
        long totalTopics = topicRepository.findByUserId(userId).size();

        double successRate = totalAnswered > 0 ? (double) totalCorrect / totalAnswered * 100 : 0;

        return UserStatsResponse.builder()
                .totalQuestions(totalQuestions)
                .totalQuizAttempts(totalAttempts)
                .totalAnswered(totalAnswered)
                .totalCorrect(totalCorrect)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .totalTopics(totalTopics)
                .build();
    }
}
