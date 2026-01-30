package com.rumoaopratico.controller;

import com.rumoaopratico.dto.request.QuizAnswerRequest;
import com.rumoaopratico.dto.request.QuizStartRequest;
import com.rumoaopratico.dto.response.QuizAnswerResponse;
import com.rumoaopratico.dto.response.QuizAttemptResponse;
import com.rumoaopratico.dto.response.QuizResultResponse;
import com.rumoaopratico.security.SecurityUtils;
import com.rumoaopratico.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz attempt and answer endpoints")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/start")
    @Operation(summary = "Start a new quiz attempt")
    public ResponseEntity<QuizAttemptResponse> startQuiz(@Valid @RequestBody QuizStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizService.startQuiz(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/{attemptId}")
    @Operation(summary = "Get quiz attempt details")
    public ResponseEntity<QuizAttemptResponse> getAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizService.getAttempt(SecurityUtils.getCurrentUserId(), attemptId));
    }

    @PostMapping("/{attemptId}/answer")
    @Operation(summary = "Submit an answer for a quiz question")
    public ResponseEntity<QuizAttemptResponse> submitAnswer(@PathVariable Long attemptId,
                                                            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(quizService.submitAnswerSimple(SecurityUtils.getCurrentUserId(), attemptId, request));
    }

    @PostMapping("/{attemptId}/finish")
    @Operation(summary = "Finish a quiz attempt")
    public ResponseEntity<QuizResultResponse> finishQuiz(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizService.finishQuiz(SecurityUtils.getCurrentUserId(), attemptId));
    }

    @GetMapping("/{attemptId}/result")
    @Operation(summary = "Get quiz result")
    public ResponseEntity<QuizResultResponse> getResult(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizService.getResult(SecurityUtils.getCurrentUserId(), attemptId));
    }
}
