package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.enums.QuizMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {
    private Long attemptId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalQuestions;
    private Integer correctCount;
    private Double successRate;
    private QuizMode mode;
    private List<QuizAnswerResponse> answers;
}
