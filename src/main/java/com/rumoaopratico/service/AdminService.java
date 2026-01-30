package com.rumoaopratico.service;

import com.rumoaopratico.dto.response.UserResponse;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.QuizAnswerRepository;
import com.rumoaopratico.repository.QuizAttemptRepository;
import com.rumoaopratico.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    public Page<UserResponse> listAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional
    public void clearUserHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        quizAnswerRepository.deleteByAttemptUserId(userId);
        quizAttemptRepository.deleteByUserId(userId);

        log.info("Cleared all quiz history for user: {} (id={})", user.getEmail(), userId);
    }
}
