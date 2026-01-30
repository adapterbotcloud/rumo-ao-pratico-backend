package com.rumoaopratico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportQuestionsRequest {

    @NotBlank(message = "Topic name is required")
    private String topicName;

    @NotNull(message = "Results are required")
    private List<ImportQuestionItem> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportQuestionItem {
        private String question;
        private String pergunta;
        private String Items;
        private String correct;
        private String correct_answer;
        private List<String> incorrect_answers;
        private String Bibliografia;
        private String questao;
    }
}
