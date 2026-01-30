package com.rumoaopratico.service;

import com.rumoaopratico.dto.user.UpdateUserRequest;
import com.rumoaopratico.dto.user.UserResponse;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.QuestionRepository;
import com.rumoaopratico.repository.QuizAttemptRepository;
import com.rumoaopratico.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, QuestionRepository questionRepository,
                       QuizAttemptRepository quizAttemptRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        long totalQuestions = questionRepository.countByUserIdAndIsActiveTrue(userId);
        long totalAttempts = quizAttemptRepository.countByUserId(userId);
        long totalCorrect = quizAttemptRepository.sumCorrectCountByUserId(userId);
        long totalAnswered = quizAttemptRepository.sumTotalQuestionsByUserId(userId);
        double accuracy = totalAnswered > 0 ? (double) totalCorrect / totalAnswered * 100.0 : 0.0;

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                totalQuestions,
                totalAttempts,
                Math.round(accuracy * 100.0) / 100.0
        );
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (StringUtils.hasText(request.name())) {
            user.setName(request.name());
        }
        if (StringUtils.hasText(request.password())) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        user = userRepository.save(user);
        return getCurrentUser(user.getId());
    }
}
