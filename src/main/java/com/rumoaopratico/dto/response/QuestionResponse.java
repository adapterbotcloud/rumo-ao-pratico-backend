package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.Question;
import com.rumoaopratico.model.enums.Difficulty;
import com.rumoaopratico.model.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private Long topicId;
    private String topicName;
    private QuestionType type;
    private String statement;
    private String explanation;
    private String bibliography;
    private Difficulty difficulty;
    private String tags;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<QuestionOptionResponse> options;

    public static QuestionResponse from(Question q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .topicId(q.getTopic().getId())
                .topicName(q.getTopic().getName())
                .type(q.getType())
                .statement(q.getStatement())
                .explanation(q.getExplanation())
                .bibliography(q.getBibliography())
                .difficulty(q.getDifficulty())
                .tags(q.getTags())
                .isActive(q.getIsActive())
                .createdAt(q.getCreatedAt())
                .options(buildOptionsWithLabels(q))
                .build();
    }

    private static List<QuestionOptionResponse> buildOptionsWithLabels(Question q) {
        if (q.getOptions() == null || q.getOptions().isEmpty()) return List.of();
        List<QuestionOptionResponse> result = new ArrayList<>();
        String[] labels = {"a", "b", "c", "d", "e", "f", "g", "h"};
        for (int i = 0; i < q.getOptions().size(); i++) {
            String label = i < labels.length ? labels[i] : String.valueOf(i + 1);
            result.add(QuestionOptionResponse.fromWithLabel(q.getOptions().get(i), label));
        }
        return result;
    }
}
