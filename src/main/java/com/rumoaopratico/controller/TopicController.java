package com.rumoaopratico.controller;

import com.rumoaopratico.dto.request.TopicRequest;
import com.rumoaopratico.dto.response.TopicResponse;
import com.rumoaopratico.security.SecurityUtils;
import com.rumoaopratico.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Tag(name = "Topics", description = "Topic management endpoints")
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    @Operation(summary = "List all topics (paginated)")
    public ResponseEntity<Page<TopicResponse>> getTopics(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(topicService.getTopics(SecurityUtils.getCurrentUserId(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a topic by ID")
    public ResponseEntity<TopicResponse> getTopic(@PathVariable Long id) {
        return ResponseEntity.ok(topicService.getTopic(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new topic")
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(topicService.createTopic(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a topic")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(topicService.updateTopic(SecurityUtils.getCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a topic")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
