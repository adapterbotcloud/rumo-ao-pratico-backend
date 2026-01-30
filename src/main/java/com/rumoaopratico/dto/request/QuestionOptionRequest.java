package com.rumoaopratico.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionRequest {

    @NotBlank(message = "Option text is required")
    private String text;

    private Boolean isCorrect;

    private String explanation;
}
