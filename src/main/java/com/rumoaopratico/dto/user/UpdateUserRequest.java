package com.rumoaopratico.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        String name,

        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password
) {}
