package com.rumoaopratico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponse {
    private Integer index;
    private Long questionId;
    private QuestionResponse question;
    private Boolean answered;
    private String userAnswer;
    private Boolean correct;
}
