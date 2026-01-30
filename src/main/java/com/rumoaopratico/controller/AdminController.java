package com.rumoaopratico.controller;

import com.rumoaopratico.dto.request.AdminUpdateUserRequest;
import com.rumoaopratico.dto.request.ImportQuestionsRequest;
import com.rumoaopratico.dto.response.ImportResultResponse;
import com.rumoaopratico.dto.response.UserResponse;
import com.rumoaopratico.security.SecurityUtils;
import com.rumoaopratico.service.AdminService;
import com.rumoaopratico.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin endpoints")
public class AdminController {

    private final ImportService importService;
    private final AdminService adminService;

    @PostMapping("/import-questions")
    @Operation(summary = "Import questions from JSON data")
    public ResponseEntity<ImportResultResponse> importQuestions(@Valid @RequestBody ImportQuestionsRequest request) {
        return ResponseEntity.ok(importService.importQuestions(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                adminService.listAllUsers(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")))
        );
    }

    @DeleteMapping("/users/{userId}/history")
    @Operation(summary = "Clear all quiz history for a user")
    public ResponseEntity<Void> clearUserHistory(@PathVariable Long userId) {
        adminService.clearUserHistory(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}")
    @Operation(summary = "Update user role and/or status (enable/disable)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(adminService.updateUser(currentUserId, userId, request));
    }
}
