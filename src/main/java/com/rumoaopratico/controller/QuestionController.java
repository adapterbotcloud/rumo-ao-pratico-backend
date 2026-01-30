package com.rumoaopratico.controller;

import com.rumoaopratico.dto.question.QuestionImportRequest;
import com.rumoaopratico.dto.question.QuestionRequest;
import com.rumoaopratico.dto.question.QuestionResponse;
import com.rumoaopratico.model.Difficulty;
import com.rumoaopratico.model.QuestionType;
import com.rumoaopratico.security.SecurityUser;
import com.rumoaopratico.service.QuestionService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/questions")
@Tag(name = "Questions", description = "Question management endpoints")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    @Operation(summary = "List questions with filters")
    public ResponseEntity<Page<QuestionResponse>> listQuestions(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) UUID topicId,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestionResponse> questions = questionService.listQuestions(
                user.getId(), topicId, type, difficulty, search, pageable);
        return ResponseEntity.ok(questions);
    }

    @PostMapping
    @Operation(summary = "Create a new question")
    public ResponseEntity<QuestionResponse> createQuestion(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody QuestionRequest request) {
        QuestionResponse response = questionService.createQuestion(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID")
    public ResponseEntity<QuestionResponse> getQuestionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        QuestionResponse response = questionService.getQuestionById(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update question")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody QuestionRequest request) {
        QuestionResponse response = questionService.updateQuestion(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete question")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        questionService.deleteQuestion(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @Operation(summary = "Bulk import questions from JSON")
    public ResponseEntity<List<QuestionResponse>> importQuestions(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody QuestionImportRequest request) {
        List<QuestionResponse> imported = questionService.importQuestions(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }
}
