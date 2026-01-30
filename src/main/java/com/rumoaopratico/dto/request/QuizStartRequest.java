package com.rumoaopratico.dto.request;

import com.rumoaopratico.model.enums.Difficulty;
import com.rumoaopratico.model.enums.QuestionType;
import com.rumoaopratico.model.enums.QuizMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
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
public class QuizStartRequest {

    @NotEmpty(message = "At least one topic must be selected")
    private List<Long> topicIds;

    @NotNull(message = "Question count is required")
    @Min(value = 1, message = "At least 1 question required")
    @Max(value = 100, message = "Maximum 100 questions per quiz")
    private Integer questionCount;

    private List<QuestionType> types;

    private Difficulty difficulty;

    @NotNull(message = "Quiz mode is required")
    private QuizMode mode;
}
