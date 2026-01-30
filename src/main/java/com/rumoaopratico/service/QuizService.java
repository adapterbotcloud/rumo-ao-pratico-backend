package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.QuizAnswerRequest;
import com.rumoaopratico.dto.request.QuizStartRequest;
import com.rumoaopratico.dto.response.*;
import com.rumoaopratico.dto.response.QuestionOptionResponse;
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

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getPendingQuizzes(Long userId) {
        return quizAttemptRepository.findByUserIdAndFinishedAtIsNullOrderByStartedAtDesc(userId)
                .stream()
                .map(attempt -> {
                    List<QuestionResponse> questions = loadQuestionsFromConfig(attempt);
                    List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attempt.getId());
                    return QuizAttemptResponse.fromWithQuestions(attempt, questions, answers);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void abandonQuiz(Long userId, Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new BadRequestException("Quiz already finished");
        }

        long correctCount = quizAnswerRepository.countByAttemptIdAndIsCorrectTrue(attemptId);
        attempt.setFinishedAt(LocalDateTime.now());
        attempt.setCorrectCount((int) correctCount);
        quizAttemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttemptResponse startQuiz(Long userId, QuizStartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Get random questions based on the criteria
        QuestionType typeFilter = null;
        if (request.getTypes() != null && request.getTypes().size() == 1) {
            typeFilter = request.getTypes().get(0);
        }

        List<Question> questions;
        boolean prioritizeUnanswered = !Boolean.TRUE.equals(request.getIncludeCorrectlyAnswered());

        if (prioritizeUnanswered) {
            // Smart selection: first try unanswered/incorrectly-answered questions
            questions = questionRepository.findUnansweredOrIncorrectForQuiz(
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
                        .collect(Collectors.toList());
            }

            // If not enough questions, fill remaining from all questions
            if (questions.size() < request.getQuestionCount()) {
                Set<Long> existingIds = questions.stream()
                        .map(Question::getId)
                        .collect(Collectors.toSet());

                int remaining = request.getQuestionCount() - questions.size();
                List<Question> additional = questionRepository.findRandomForQuizGlobal(
                        request.getTopicIds(),
                        typeFilter,
                        request.getDifficulty(),
                        PageRequest.of(0, remaining + existingIds.size())
                );

                // Filter out duplicates and apply type filter
                List<Question> filtered = additional.stream()
                        .filter(q -> !existingIds.contains(q.getId()))
                        .filter(q -> request.getTypes() == null || request.getTypes().size() <= 1 || request.getTypes().contains(q.getType()))
                        .limit(remaining)
                        .collect(Collectors.toList());

                questions = new ArrayList<>(questions);
                questions.addAll(filtered);
            }

            // Trim to requested count
            if (questions.size() > request.getQuestionCount()) {
                questions = questions.subList(0, request.getQuestionCount());
            }
        } else {
            // Include all questions (existing behavior)
            questions = questionRepository.findRandomForQuizGlobal(
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

        return QuizAttemptResponse.fromWithQuestions(attempt, questionResponses, List.of());
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponse getAttempt(Long userId, Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        // Load questions from config
        List<QuestionResponse> questions = loadQuestionsFromConfig(attempt);
        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attemptId);

        return QuizAttemptResponse.fromWithQuestions(attempt, questions, answers);
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
    public QuizAttemptResponse submitAnswerSimple(Long userId, Long attemptId, Map<String, Object> request) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new BadRequestException("Quiz already finished");
        }

        // Find current question index (first unanswered)
        List<QuestionResponse> questions = loadQuestionsFromConfig(attempt);
        List<QuizAnswer> existingAnswers = quizAnswerRepository.findByAttemptId(attemptId);
        Set<Long> answeredQuestionIds = existingAnswers.stream()
                .map(a -> a.getQuestion().getId())
                .collect(Collectors.toSet());

        // Find current question
        QuestionResponse currentQuestion = null;
        for (QuestionResponse qr : questions) {
            if (!answeredQuestionIds.contains(qr.getId())) {
                currentQuestion = qr;
                break;
            }
        }

        if (currentQuestion == null) {
            throw new BadRequestException("All questions already answered");
        }

        final Long currentQuestionId = currentQuestion.getId();
        Question question = questionRepository.findById(currentQuestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", currentQuestionId));

        // Parse the answer - frontend sends { answer: "a" } or { answer: "true" }
        String answerValue = request.get("answer") != null ? request.get("answer").toString() : "";

        // Build answer map for evaluation
        Map<String, Object> answerMap = new HashMap<>();
        if (answerValue.matches("[a-h]")) {
            // Multiple choice - find option by label index
            int idx = answerValue.charAt(0) - 'a';
            if (idx >= 0 && idx < question.getOptions().size()) {
                answerMap.put("selectedOptionId", question.getOptions().get(idx).getId());
            }
        } else if (answerValue.equals("true") || answerValue.equals("false")) {
            // TRUE_FALSE or COMMENTED_PHRASE - match by option text
            if (!question.getOptions().isEmpty()) {
                QuestionOption matched = null;
                for (QuestionOption opt : question.getOptions()) {
                    String optText = opt.getText().toLowerCase().trim();
                    if (answerValue.equals("true") && (optText.equals("verdadeiro") || optText.equals("true") || optText.equals("correta") || optText.equals("correto"))) {
                        matched = opt;
                        break;
                    } else if (answerValue.equals("false") && (optText.equals("falso") || optText.equals("false") || optText.equals("incorreta") || optText.equals("incorreto"))) {
                        matched = opt;
                        break;
                    }
                }
                // Fallback to index-based if no text match
                if (matched == null) {
                    int selectedIdx = answerValue.equals("true") ? 0 : Math.min(1, question.getOptions().size() - 1);
                    matched = question.getOptions().get(selectedIdx);
                }
                answerMap.put("selectedOptionId", matched.getId());
            }
        } else if (answerValue.equals("correct") || answerValue.equals("wrong")) {
            // FLASHCARD - self-assessment: user reports if they got it right
            answerMap.put("selfAssessment", answerValue);
        } else {
            answerMap.put("answer", answerValue);
        }

        boolean isCorrect;
        if (answerMap.containsKey("selfAssessment")) {
            // Flashcard: user self-reports correctness
            isCorrect = "correct".equals(answerMap.get("selfAssessment"));
        } else {
            isCorrect = evaluateAnswer(question, answerMap);
        }

        QuizAnswer answer = QuizAnswer.builder()
                .attempt(attempt)
                .question(question)
                .userAnswerJson(answerMap)
                .isCorrect(isCorrect)
                .build();

        quizAnswerRepository.save(answer);

        if (isCorrect) {
            attempt.setCorrectCount(attempt.getCorrectCount() + 1);
            quizAttemptRepository.save(attempt);
        }

        // Return updated attempt
        List<QuizAnswer> allAnswers = quizAnswerRepository.findByAttemptId(attemptId);
        return QuizAttemptResponse.fromWithQuestions(attempt, questions, allAnswers);
    }

    @Transactional
    public QuizResultResponse finishQuiz(Long userId, Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));

        if (attempt.getFinishedAt() != null) {
            throw new BadRequestException("Quiz already finished");
        }

        long correctCount = quizAnswerRepository.countByAttemptIdAndIsCorrectTrue(attemptId);

        attempt.setFinishedAt(LocalDateTime.now());
        attempt.setCorrectCount((int) correctCount);
        quizAttemptRepository.save(attempt);

        return buildQuizResult(attempt);
    }

    @Transactional(readOnly = true)
    public QuizResultResponse getResult(Long userId, Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt", attemptId));
        return buildQuizResult(attempt);
    }

    private QuizResultResponse buildQuizResult(QuizAttempt attempt) {
        Long attemptId = attempt.getId();
        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attemptId);
        List<QuestionResponse> questionResponses = loadQuestionsFromConfig(attempt);

        int correctCount = attempt.getCorrectCount() != null ? attempt.getCorrectCount() : 0;
        int total = attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : 0;
        double score = total > 0 ? (double) correctCount / total * 100 : 0;

        // Calculate time
        long totalTimeSeconds = 0;
        if (attempt.getStartedAt() != null) {
            LocalDateTime end = attempt.getFinishedAt() != null ? attempt.getFinishedAt() : LocalDateTime.now();
            totalTimeSeconds = java.time.Duration.between(attempt.getStartedAt(), end).getSeconds();
        }

        // Build questions list matching frontend format
        List<QuizResultResponse.QuizResultQuestionResponse> resultQuestions = new ArrayList<>();
        for (int i = 0; i < questionResponses.size(); i++) {
            QuestionResponse qr = questionResponses.get(i);
            QuizAnswer matchingAnswer = answers.stream()
                    .filter(a -> a.getQuestion().getId().equals(qr.getId()))
                    .findFirst().orElse(null);

            String userAnswer = "";
            boolean isCorrect = false;
            if (matchingAnswer != null) {
                isCorrect = Boolean.TRUE.equals(matchingAnswer.getIsCorrect());
                userAnswer = extractLabel(matchingAnswer, qr);
            }

            resultQuestions.add(QuizResultResponse.QuizResultQuestionResponse.builder()
                    .index(i)
                    .questionId(qr.getId())
                    .question(qr)
                    .userAnswer(userAnswer)
                    .correct(isCorrect)
                    .build());
        }

        // Breakdowns
        Map<String, QuizResultResponse.BreakdownEntry> byTopic = new LinkedHashMap<>();
        Map<String, QuizResultResponse.BreakdownEntry> byType = new LinkedHashMap<>();
        for (var rq : resultQuestions) {
            String topic = rq.getQuestion().getTopicName();
            String type = rq.getQuestion().getType().name();
            byTopic.computeIfAbsent(topic, k -> new QuizResultResponse.BreakdownEntry(0, 0, 0));
            byType.computeIfAbsent(type, k -> new QuizResultResponse.BreakdownEntry(0, 0, 0));
            QuizResultResponse.BreakdownEntry te = byTopic.get(topic);
            te.setTotal(te.getTotal() + 1);
            if (rq.isCorrect()) te.setCorrect(te.getCorrect() + 1);
            QuizResultResponse.BreakdownEntry tye = byType.get(type);
            tye.setTotal(tye.getTotal() + 1);
            if (rq.isCorrect()) tye.setCorrect(tye.getCorrect() + 1);
        }
        byTopic.values().forEach(e -> e.setScore(e.getTotal() > 0 ? (double) e.getCorrect() / e.getTotal() * 100 : 0));
        byType.values().forEach(e -> e.setScore(e.getTotal() > 0 ? (double) e.getCorrect() / e.getTotal() * 100 : 0));

        return QuizResultResponse.builder()
                .attemptId(attemptId)
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .totalQuestions(total)
                .correctAnswers(correctCount)
                .correctCount(correctCount)
                .score(Math.round(score * 100.0) / 100.0)
                .successRate(Math.round(score * 100.0) / 100.0)
                .totalTimeSeconds(totalTimeSeconds)
                .mode(attempt.getMode())
                .questions(resultQuestions)
                .breakdownByTopic(byTopic)
                .breakdownByType(byType)
                .build();
    }

    private String extractLabel(QuizAnswer answer, QuestionResponse questionResponse) {
        if (answer.getUserAnswerJson() == null) return "";
        Object selectedOptionId = answer.getUserAnswerJson().get("selectedOptionId");
        if (selectedOptionId != null) {
            long optId = selectedOptionId instanceof Number
                    ? ((Number) selectedOptionId).longValue()
                    : Long.parseLong(selectedOptionId.toString());

            // For TRUE_FALSE and COMMENTED_PHRASE, return "true"/"false" instead of label
            QuestionType qType = questionResponse.getType();
            if (qType == QuestionType.TRUE_FALSE || qType == QuestionType.COMMENTED_PHRASE) {
                List<QuestionOptionResponse> options = questionResponse.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    if (options.get(i).getId().equals(optId)) {
                        return i == 0 ? "true" : "false";
                    }
                }
            }

            for (var opt : questionResponse.getOptions()) {
                if (opt.getId().equals(optId) && opt.getLabel() != null) {
                    return opt.getLabel();
                }
            }
            return selectedOptionId.toString();
        }
        Object answerText = answer.getUserAnswerJson().get("answer");
        return answerText != null ? answerText.toString() : "";
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
