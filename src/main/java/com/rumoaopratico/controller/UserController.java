package com.rumoaopratico.controller;

import com.rumoaopratico.dto.request.UpdateUserRequest;
import com.rumoaopratico.dto.response.UserResponse;
import com.rumoaopratico.dto.response.UserStatsResponse;
import com.rumoaopratico.security.SecurityUtils;
import com.rumoaopratico.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and stats endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/me/stats")
    @Operation(summary = "Get current user statistics")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        return ResponseEntity.ok(userService.getUserStats(SecurityUtils.getCurrentUserId()));
    }
}
