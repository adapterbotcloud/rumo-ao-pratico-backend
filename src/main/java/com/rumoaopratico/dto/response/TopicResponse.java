package com.rumoaopratico.dto.response;

import com.rumoaopratico.model.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdAt;

    public static TopicResponse from(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .parentId(topic.getParent() != null ? topic.getParent().getId() : null)
                .parentName(topic.getParent() != null ? topic.getParent().getName() : null)
                .createdAt(topic.getCreatedAt())
                .build();
    }
}
