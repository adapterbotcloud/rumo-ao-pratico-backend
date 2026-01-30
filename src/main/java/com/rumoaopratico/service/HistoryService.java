package com.rumoaopratico.service;

import com.rumoaopratico.dto.response.QuizAnswerResponse;
import com.rumoaopratico.dto.response.QuizAttemptResponse;
import com.rumoaopratico.dto.response.QuizResultResponse;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.QuizAnswer;
import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.repository.QuizAnswerRepository;
import com.rumoaopratico.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    @Transactional(readOnly = true)
    public Page<QuizAttemptResponse> getHistory(Long userId, LocalDateTime startDate,
                                                 LocalDateTime endDate, Pageable pageable) {
        return quizAttemptRepository.findByUserIdFiltered(userId, startDate, endDate, pageable)
                .map(QuizAttemptResponse::from);
    }

    @Transactional(readOnly = true)
    public QuizResultResponse getHistoryDetail(Long userId, Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attemptId);
        List<QuizAnswerResponse> answerResponses = answers.stream()
                .map(QuizAnswerResponse::fromWithQuestion)
                .collect(Collectors.toList());

        double successRate = attempt.getTotalQuestions() != null && attempt.getTotalQuestions() > 0
                ? (double) (attempt.getCorrectCount() != null ? attempt.getCorrectCount() : 0)
                  / attempt.getTotalQuestions() * 100 : 0;

        return QuizResultResponse.builder()
                .attemptId(attemptId)
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .mode(attempt.getMode())
                .answers(answerResponses)
                .build();
    }
}
