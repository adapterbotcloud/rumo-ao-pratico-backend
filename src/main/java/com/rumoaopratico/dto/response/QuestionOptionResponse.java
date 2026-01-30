package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.QuestionOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionResponse {
    private Long id;
    private String text;
    private Boolean isCorrect;
    private String explanation;

    public static QuestionOptionResponse from(QuestionOption opt) {
        return QuestionOptionResponse.builder()
                .id(opt.getId())
                .text(opt.getText())
                .isCorrect(opt.getIsCorrect())
                .explanation(opt.getExplanation())
                .build();
    }
}
