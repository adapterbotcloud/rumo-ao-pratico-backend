package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.QuizAnswerRequest;
import com.rumoaopratico.dto.request.QuizStartRequest;
import com.rumoaopratico.dto.response.*;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.*;
import com.rumoaopratico.model.enums.QuestionType;
import com.rumoaopratico.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizAttemptResponse startQuiz(Long userId, QuizStartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Get random questions based on the criteria
        QuestionType typeFilter = null;
        if (request.getTypes() != null && request.getTypes().size() == 1) {
            typeFilter = request.getTypes().get(0);
        }

        List<Question> questions = questionRepository.findRandomForQuiz(
                userId,
                request.getTopicIds(),
                typeFilter,
                request.getDifficulty(),
                PageRequest.of(0, request.getQuestionCount())
        );

        // If multiple types specified, filter in memory
        if (request.getTypes() != null && request.getTypes().size() > 1) {
            questions = questions.stream()
                    .filter(q -> request.getTypes().contains(q.getType()))
                    .limit(request.getQuestionCount())
                    .collect(Collectors.toList());
        }

        if (questions.isEmpty()) {
            throw new BadRequestException("No questions found matching the selected criteria");
        }

        // Build config JSON
        Map<String, Object> config = new HashMap<>();
        config.put("topicIds", request.getTopicIds());
        config.put("requestedCount", request.getQuestionCount());
        config.put("types", request.getTypes());
        config.put("difficulty", request.getDifficulty());
        config.put("questionIds", questions.stream().map(Question::getId).collect(Collectors.toList()));

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .totalQuestions(questions.size())
                .correctCount(0)
                .mode(request.getMode())
                .configJson(config)
                .answers(new ArrayList<>())
                .build();

        attempt = quizAttemptRepository.save(attempt);

        List<QuestionResponse> questionResponses = questions.stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());

        return QuizAttemptResponse.fromWithQuestions(attempt, questionResponses);
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponse getAttempt(Long userId, Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        // Load questions from config
        List<QuestionResponse> questions = loadQuestionsFromConfig(attempt);

        return QuizAttemptResponse.fromWithQuestions(attempt, questions);
    }

    @Transactional
    public QuizAnswerResponse submitAnswer(Long userId, Long attemptId, QuizAnswerRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new BadRequestException("Quiz already finished");
        }

        if (quizAnswerRepository.existsByAttemptIdAndQuestionId(attemptId, request.getQuestionId())) {
            throw new BadRequestException("Question already answered in this attempt");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", request.getQuestionId()));

        // Check if answer is correct
        boolean isCorrect = evaluateAnswer(question, request.getAnswer());

        QuizAnswer answer = QuizAnswer.builder()
                .attempt(attempt)
                .question(question)
                .userAnswerJson(request.getAnswer())
                .isCorrect(isCorrect)
                .build();

        answer = quizAnswerRepository.save(answer);

        // Update correct count
        if (isCorrect) {
            attempt.setCorrectCount(attempt.getCorrectCount() + 1);
            quizAttemptRepository.save(attempt);
        }

        return QuizAnswerResponse.fromWithQuestion(answer);
    }

    @Transactional
    public QuizResultResponse finishQuiz(Long userId, Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new BadRequestException("Quiz already finished");
        }

        long answeredCount = quizAnswerRepository.countByAttemptId(attemptId);
        long correctCount = quizAnswerRepository.countByAttemptIdAndIsCorrectTrue(attemptId);

        attempt.setFinishedAt(LocalDateTime.now());
        attempt.setCorrectCount((int) correctCount);
        quizAttemptRepository.save(attempt);

        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attemptId);
        List<QuizAnswerResponse> answerResponses = answers.stream()
                .map(QuizAnswerResponse::fromWithQuestion)
                .collect(Collectors.toList());

        double successRate = attempt.getTotalQuestions() > 0
                ? (double) correctCount / attempt.getTotalQuestions() * 100 : 0;

        return QuizResultResponse.builder()
                .attemptId(attemptId)
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount((int) correctCount)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .mode(attempt.getMode())
                .answers(answerResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public QuizResultResponse getResult(Long userId, Long attemptId) {
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

    private boolean evaluateAnswer(Question question, Map<String, Object> answer) {
        Object selectedOptionId = answer.get("selectedOptionId");
        if (selectedOptionId != null) {
            long optId;
            if (selectedOptionId instanceof Number) {
                optId = ((Number) selectedOptionId).longValue();
            } else {
                optId = Long.parseLong(selectedOptionId.toString());
            }

            return question.getOptions().stream()
                    .anyMatch(opt -> opt.getId().equals(optId) && Boolean.TRUE.equals(opt.getIsCorrect()));
        }

        // For text-based answers
        Object answerText = answer.get("answer");
        if (answerText != null) {
            return question.getOptions().stream()
                    .anyMatch(opt -> Boolean.TRUE.equals(opt.getIsCorrect())
                            && opt.getText().equalsIgnoreCase(answerText.toString()));
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private List<QuestionResponse> loadQuestionsFromConfig(QuizAttempt attempt) {
        if (attempt.getConfigJson() != null && attempt.getConfigJson().containsKey("questionIds")) {
            List<Number> questionIds = (List<Number>) attempt.getConfigJson().get("questionIds");
            List<Long> ids = questionIds.stream().map(Number::longValue).collect(Collectors.toList());
            return questionRepository.findAllById(ids).stream()
                    .map(QuestionResponse::from)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
