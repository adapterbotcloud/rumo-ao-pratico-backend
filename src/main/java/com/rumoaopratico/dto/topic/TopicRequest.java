package com.rumoaopratico.dto.topic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TopicRequest(
        @NotBlank(message = "Topic name is required")
        @Size(max = 255, message = "Topic name must not exceed 255 characters")
        String name,

        UUID parentId,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {}
