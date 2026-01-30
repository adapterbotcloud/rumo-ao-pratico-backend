package com.rumoaopratico.dto.request;

import com.rumoaopratico.model.enums.Difficulty;
import com.rumoaopratico.model.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotNull(message = "Topic ID is required")
    private Long topicId;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    @NotBlank(message = "Statement is required")
    private String statement;

    private String explanation;

    @Size(max = 500)
    private String bibliography;

    private Difficulty difficulty;

    @Size(max = 500)
    private String tags;

    @Valid
    private List<QuestionOptionRequest> options;
}
