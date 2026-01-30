package com.rumoaopratico.controller;

import com.rumoaopratico.dto.response.QuizAttemptResponse;
import com.rumoaopratico.dto.response.QuizResultResponse;
import com.rumoaopratico.security.SecurityUtils;
import com.rumoaopratico.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "Quiz history endpoints")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @Operation(summary = "Get quiz history (paginated, filterable)")
    public ResponseEntity<Page<QuizAttemptResponse>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(historyService.getHistory(
                SecurityUtils.getCurrentUserId(), startDate, endDate, pageable));
    }

    @GetMapping("/{attemptId}")
    @Operation(summary = "Get detailed quiz history for an attempt")
    public ResponseEntity<QuizResultResponse> getHistoryDetail(@PathVariable Long attemptId) {
        return ResponseEntity.ok(historyService.getHistoryDetail(SecurityUtils.getCurrentUserId(), attemptId));
    }
}
