package com.rumoaopratico.controller;

import com.rumoaopratico.dto.topic.TopicRequest;
import com.rumoaopratico.dto.topic.TopicResponse;
import com.rumoaopratico.security.SecurityUser;
import com.rumoaopratico.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/topics")
@Tag(name = "Topics", description = "Topic management endpoints")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    @Operation(summary = "List user's topics (paginated, tree structure)")
    public ResponseEntity<Page<TopicResponse>> listTopics(
            @AuthenticationPrincipal SecurityUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TopicResponse> topics = topicService.listTopics(user.getId(), pageable);
        return ResponseEntity.ok(topics);
    }

    @PostMapping
    @Operation(summary = "Create a new topic")
    public ResponseEntity<TopicResponse> createTopic(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody TopicRequest request) {
        TopicResponse response = topicService.createTopic(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get topic by ID")
    public ResponseEntity<TopicResponse> getTopicById(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        TopicResponse response = topicService.getTopicById(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update topic")
    public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody TopicRequest request) {
        TopicResponse response = topicService.updateTopic(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete topic")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser user) {
        topicService.deleteTopic(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
