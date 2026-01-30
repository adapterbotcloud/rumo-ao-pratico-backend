package com.rumoaopratico.controller;

import com.rumoaopratico.dto.request.QuestionRequest;
import com.rumoaopratico.dto.response.QuestionResponse;
import com.rumoaopratico.model.enums.Difficulty;
import com.rumoaopratico.model.enums.QuestionType;
import com.rumoaopratico.security.SecurityUtils;
import com.rumoaopratico.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Question management endpoints")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    @Operation(summary = "List questions with filters (paginated)")
    public ResponseEntity<Page<QuestionResponse>> getQuestions(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(questionService.getQuestions(
                SecurityUtils.getCurrentUserId(), topicId, type, difficulty, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a question by ID")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestion(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new question with options")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a question")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(SecurityUtils.getCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a question")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
