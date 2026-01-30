package com.rumoaopratico.controller;

import com.rumoaopratico.dto.user.UpdateUserRequest;
import com.rumoaopratico.dto.user.UserResponse;
import com.rumoaopratico.security.SecurityUser;
import com.rumoaopratico.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile with stats")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal SecurityUser user) {
        UserResponse response = userService.getCurrentUser(user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(user.getId(), request);
        return ResponseEntity.ok(response);
    }
}
