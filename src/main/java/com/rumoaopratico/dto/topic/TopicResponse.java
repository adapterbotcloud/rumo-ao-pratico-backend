package com.rumoaopratico.dto.topic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TopicResponse(
        UUID id,
        String name,
        UUID parentId,
        String description,
        List<TopicResponse> children,
        Long questionCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
