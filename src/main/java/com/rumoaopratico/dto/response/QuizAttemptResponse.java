package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.model.QuizAnswer;
import com.rumoaopratico.model.enums.QuizMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptResponse {
    private Long id;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalQuestions;
    private Integer correctCount;
    private QuizMode mode;
    private String status;
    private Integer currentQuestionIndex;
    private Map<String, Object> config;
    private List<QuizQuestionResponse> questions;

    public static QuizAttemptResponse from(QuizAttempt attempt) {
        String status = attempt.getFinishedAt() != null ? "COMPLETED" : "IN_PROGRESS";

        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .mode(attempt.getMode())
                .status(status)
                .currentQuestionIndex(0)
                .config(attempt.getConfigJson())
                .build();
    }

    public static QuizAttemptResponse fromWithQuestions(
            QuizAttempt attempt,
            List<QuestionResponse> questionResponses,
            List<QuizAnswer> answers) {

        QuizAttemptResponse response = from(attempt);

        List<QuizQuestionResponse> quizQuestions = new ArrayList<>();
        for (int i = 0; i < questionResponses.size(); i++) {
            QuestionResponse qr = questionResponses.get(i);
            QuizAnswer matchingAnswer = answers != null
                    ? answers.stream()
                        .filter(a -> a.getQuestion().getId().equals(qr.getId()))
                        .findFirst().orElse(null)
                    : null;

            quizQuestions.add(QuizQuestionResponse.builder()
                    .index(i)
                    .questionId(qr.getId())
                    .question(qr)
                    .answered(matchingAnswer != null)
                    .userAnswer(matchingAnswer != null ? extractUserAnswer(matchingAnswer) : null)
                    .correct(matchingAnswer != null ? matchingAnswer.getIsCorrect() : null)
                    .build());
        }

        response.setQuestions(quizQuestions);

        // Set currentQuestionIndex to first unanswered
        int currentIndex = 0;
        for (int i = 0; i < quizQuestions.size(); i++) {
            if (!Boolean.TRUE.equals(quizQuestions.get(i).getAnswered())) {
                currentIndex = i;
                break;
            }
            if (i == quizQuestions.size() - 1) {
                currentIndex = quizQuestions.size(); // all answered
            }
        }
        response.setCurrentQuestionIndex(currentIndex);

        return response;
    }

    @SuppressWarnings("unchecked")
    private static String extractUserAnswer(QuizAnswer answer) {
        if (answer.getUserAnswerJson() == null) return null;
        Map<String, Object> answerMap = answer.getUserAnswerJson();
        Object selectedOptionId = answerMap.get("selectedOptionId");
        if (selectedOptionId != null) return selectedOptionId.toString();
        Object answerText = answerMap.get("answer");
        if (answerText != null) return answerText.toString();
        return answerMap.toString();
    }
}
