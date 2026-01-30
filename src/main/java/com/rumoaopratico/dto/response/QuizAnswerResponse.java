package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.QuizAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerResponse {
    private Long id;
    private Long questionId;
    private Map<String, Object> userAnswer;
    private Boolean isCorrect;
    private LocalDateTime answeredAt;
    private QuestionResponse question;

    public static QuizAnswerResponse from(QuizAnswer answer) {
        return QuizAnswerResponse.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion().getId())
                .userAnswer(answer.getUserAnswerJson())
                .isCorrect(answer.getIsCorrect())
                .answeredAt(answer.getAnsweredAt())
                .build();
    }

    public static QuizAnswerResponse fromWithQuestion(QuizAnswer answer) {
        QuizAnswerResponse response = from(answer);
        response.setQuestion(QuestionResponse.from(answer.getQuestion()));
        return response;
    }
}
