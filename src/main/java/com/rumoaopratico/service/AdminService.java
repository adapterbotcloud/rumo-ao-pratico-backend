package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.AdminUpdateUserRequest;
import com.rumoaopratico.dto.response.UserResponse;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.User;
import com.rumoaopratico.model.enums.Role;
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

    @Transactional
    public UserResponse updateUser(Long currentUserId, Long targetUserId, AdminUpdateUserRequest request) {
        if (currentUserId.equals(targetUserId)) {
            throw new BadRequestException("You cannot modify your own account");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        if (request.getRole() != null) {
            try {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        user = userRepository.save(user);
        log.info("Admin (id={}) updated user: {} (id={}) - role={}, enabled={}",
                currentUserId, user.getEmail(), user.getId(), user.getRole(), user.getEnabled());

        return UserResponse.from(user);
    }
}
