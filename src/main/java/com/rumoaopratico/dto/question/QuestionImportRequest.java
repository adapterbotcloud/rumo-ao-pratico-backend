package com.rumoaopratico.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record QuestionImportRequest(
        @NotNull(message = "Topic ID is required")
        UUID topicId,

        @NotNull(message = "Results are required")
        List<ImportItem> results
) {
    public record ImportItem(
            @JsonProperty("Bibliografia")
            String bibliografia,

            @JsonProperty("Items")
            String items,

            @JsonProperty("correct")
            String correct,

            @JsonProperty("correct_answer")
            String correctAnswer,

            @JsonProperty("incorrect_answers")
            List<String> incorrectAnswers,

            @JsonProperty("pergunta")
            String pergunta,

            @JsonProperty("questao")
            String questao,

            @JsonProperty("question")
            String question
    ) {}
}
