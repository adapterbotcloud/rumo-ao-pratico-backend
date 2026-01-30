package com.rumoaopratico.service;

import com.rumoaopratico.dto.question.QuestionResponse;
import com.rumoaopratico.dto.quiz.*;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.*;
import com.rumoaopratico.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;

    public QuizService(QuizAttemptRepository quizAttemptRepository, QuizAnswerRepository quizAnswerRepository,
                       QuestionRepository questionRepository, UserRepository userRepository,
                       QuestionService questionService) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.questionService = questionService;
    }

    @Transactional
    public QuizAttemptResponse generateQuiz(UUID userId, QuizGenerateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<UUID> topicIds = request.topicIds();
        List<QuestionType> types = request.types();

        List<Question> questions = questionRepository.findRandomQuestions(
                userId,
                topicIds != null && !topicIds.isEmpty() ? topicIds : null,
                types != null && !types.isEmpty() ? types : null,
                request.difficulty(),
                PageRequest.of(0, request.count())
        );

        if (questions.isEmpty()) {
            throw new BadRequestException("No questions found matching the criteria");
        }

        Map<String, Object> configMap = new HashMap<>();
        if (topicIds != null) configMap.put("topicIds", topicIds);
        if (types != null) configMap.put("types", types);
        if (request.difficulty() != null) configMap.put("difficulty", request.difficulty().name());
        configMap.put("requestedCount", request.count());

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .startedAt(LocalDateTime.now())
                .totalQuestions(questions.size())
                .correctCount(0)
                .mode(request.mode())
                .configJson(configMap)
                .build();

        attempt = quizAttemptRepository.save(attempt);

        List<QuestionResponse> questionResponses = questions.stream()
                .map(questionService::toResponse)
                .collect(Collectors.toList());

        return toAttemptResponse(attempt, new ArrayList<>(), questionResponses);
    }

    public Page<QuizAttemptResponse> listAttempts(UUID userId, QuizMode mode, Pageable pageable) {
        return quizAttemptRepository.findByUserIdWithFilters(userId, mode, pageable)
                .map(attempt -> {
                    List<QuizAnswerResponse> answers = quizAnswerRepository.findByAttemptId(attempt.getId())
                            .stream().map(this::toAnswerResponse).collect(Collectors.toList());
                    return toAttemptResponse(attempt, answers, null);
                });
    }

    public QuizAttemptResponse getAttemptById(UUID attemptId, UUID userId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        List<QuizAnswerResponse> answers = quizAnswerRepository.findByAttemptId(attemptId)
                .stream().map(this::toAnswerResponse).collect(Collectors.toList());

        List<QuestionResponse> questions = attempt.getAnswers().stream()
                .map(a -> questionService.toResponse(a.getQuestion()))
                .collect(Collectors.toList());

        return toAttemptResponse(attempt, answers, questions);
    }

    @Transactional
    public QuizAnswerResponse submitAnswer(UUID attemptId, UUID userId, QuizAnswerRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new BadRequestException("Quiz attempt is already finished");
        }

        if (quizAnswerRepository.existsByAttemptIdAndQuestionId(attemptId, request.questionId())) {
            throw new BadRequestException("Question already answered in this attempt");
        }

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", request.questionId()));

        // Determine correctness
        boolean isCorrect = evaluateAnswer(question, request.userAnswer());

        QuizAnswer answer = QuizAnswer.builder()
                .attempt(attempt)
                .question(question)
                .userAnswerJson(request.userAnswer())
                .isCorrect(isCorrect)
                .answeredAt(LocalDateTime.now())
                .build();

        answer = quizAnswerRepository.save(answer);

        if (isCorrect) {
            attempt.setCorrectCount(attempt.getCorrectCount() + 1);
            quizAttemptRepository.save(attempt);
        }

        return toAnswerResponse(answer);
    }

    @Transactional
    public QuizAttemptResponse finishAttempt(UUID attemptId, UUID userId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new BadRequestException("Quiz attempt is already finished");
        }

        attempt.setFinishedAt(LocalDateTime.now());

        // Recalculate correct count from answers
        long correctCount = quizAnswerRepository.countCorrectByAttemptId(attemptId);
        attempt.setCorrectCount((int) correctCount);

        attempt = quizAttemptRepository.save(attempt);

        List<QuizAnswerResponse> answers = quizAnswerRepository.findByAttemptId(attemptId)
                .stream().map(this::toAnswerResponse).collect(Collectors.toList());

        return toAttemptResponse(attempt, answers, null);
    }

    private boolean evaluateAnswer(Question question, Map<String, Object> userAnswer) {
        if (userAnswer == null || !userAnswer.containsKey("selectedOptionId")) {
            return false;
        }

        String selectedOptionId = userAnswer.get("selectedOptionId").toString();
        return question.getOptions().stream()
                .anyMatch(opt -> opt.getId().toString().equals(selectedOptionId) && opt.getIsCorrect());
    }

    private QuizAttemptResponse toAttemptResponse(QuizAttempt attempt, List<QuizAnswerResponse> answers,
                                                   List<QuestionResponse> questions) {
        return new QuizAttemptResponse(
                attempt.getId(),
                attempt.getStartedAt(),
                attempt.getFinishedAt(),
                attempt.getTotalQuestions(),
                attempt.getCorrectCount(),
                attempt.getMode(),
                attempt.getConfigJson(),
                answers,
                questions,
                attempt.getCreatedAt()
        );
    }

    private QuizAnswerResponse toAnswerResponse(QuizAnswer answer) {
        return new QuizAnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getUserAnswerJson(),
                answer.getIsCorrect(),
                answer.getAnsweredAt()
        );
    }
}
