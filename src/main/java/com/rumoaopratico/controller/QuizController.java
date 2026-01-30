package com.rumoaopratico.controller;

import com.rumoaopratico.dto.quiz.QuizAnswerRequest;
import com.rumoaopratico.dto.quiz.QuizAnswerResponse;
import com.rumoaopratico.dto.quiz.QuizAttemptResponse;
import com.rumoaopratico.dto.quiz.QuizGenerateRequest;
import com.rumoaopratico.model.QuizMode;
import com.rumoaopratico.security.SecurityUser;
import com.rumoaopratico.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/quiz")
@Tag(name = "Quiz", description = "Quiz attempt endpoints")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate a new quiz")
    public ResponseEntity<QuizAttemptResponse> generateQuiz(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody QuizGenerateRequest request) {
        QuizAttemptResponse response = quizService.generateQuiz(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/attempts")
    @Operation(summary = "List user's quiz attempts")
    public ResponseEntity<Page<QuizAttemptResponse>> listAttempts(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) QuizMode mode,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuizAttemptResponse> attempts = quizService.listAttempts(user.getId(), mode, pageable);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/attempts/{id}")
    @Operation(summary = "Get quiz attempt details")
    public ResponseEntity<QuizAttemptResponse> getAttempt(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        QuizAttemptResponse response = quizService.getAttemptById(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attempts/{id}/answer")
    @Operation(summary = "Submit an answer for a question in the quiz")
    public ResponseEntity<QuizAnswerResponse> submitAnswer(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody QuizAnswerRequest request) {
        QuizAnswerResponse response = quizService.submitAnswer(id, user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/attempts/{id}/finish")
    @Operation(summary = "Finish a quiz attempt")
    public ResponseEntity<QuizAttemptResponse> finishAttempt(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        QuizAttemptResponse response = quizService.finishAttempt(id, user.getId());
        return ResponseEntity.ok(response);
    }
}
