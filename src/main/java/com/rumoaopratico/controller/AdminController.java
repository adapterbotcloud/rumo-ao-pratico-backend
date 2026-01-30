package com.rumoaopratico.controller;

import com.rumoaopratico.dto.request.ImportQuestionsRequest;
import com.rumoaopratico.dto.response.ImportResultResponse;
import com.rumoaopratico.security.SecurityUtils;
import com.rumoaopratico.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin import endpoints")
public class AdminController {

    private final ImportService importService;

    @PostMapping("/import-questions")
    @Operation(summary = "Import questions from JSON data")
    public ResponseEntity<ImportResultResponse> importQuestions(@Valid @RequestBody ImportQuestionsRequest request) {
        return ResponseEntity.ok(importService.importQuestions(SecurityUtils.getCurrentUserId(), request));
    }
}
