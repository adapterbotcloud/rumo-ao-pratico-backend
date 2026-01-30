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
    private String label;
    private String text;
    private Boolean isCorrect;
    private Boolean correct;
    private String explanation;

    public static QuestionOptionResponse from(QuestionOption opt) {
        return QuestionOptionResponse.builder()
                .id(opt.getId())
                .text(opt.getText())
                .isCorrect(opt.getIsCorrect())
                .correct(opt.getIsCorrect())
                .explanation(opt.getExplanation())
                .build();
    }

    public static QuestionOptionResponse fromWithLabel(QuestionOption opt, String label) {
        return QuestionOptionResponse.builder()
                .id(opt.getId())
                .label(label)
                .text(opt.getText())
                .isCorrect(opt.getIsCorrect())
                .correct(opt.getIsCorrect())
                .explanation(opt.getExplanation())
                .build();
    }
}
