package com.rumoaopratico.service;

import com.rumoaopratico.dto.topic.TopicRequest;
import com.rumoaopratico.dto.topic.TopicResponse;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.Topic;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.TopicRepository;
import com.rumoaopratico.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public TopicService(TopicRepository topicRepository, UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    public Page<TopicResponse> listTopics(UUID userId, Pageable pageable) {
        return topicRepository.findByUserIdAndParentIsNull(userId, pageable)
                .map(this::toResponseWithChildren);
    }

    public TopicResponse getTopicById(UUID topicId, UUID userId) {
        Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", topicId));
        return toResponseWithChildren(topic);
    }

    @Transactional
    public TopicResponse createTopic(UUID userId, TopicRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Topic topic = Topic.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .build();

        if (request.parentId() != null) {
            Topic parent = topicRepository.findByIdAndUserId(request.parentId(), userId)
                    .orElseThrow(() -> new BadRequestException("Parent topic not found: " + request.parentId()));
            topic.setParent(parent);
        }

        topic = topicRepository.save(topic);
        return toResponse(topic);
    }

    @Transactional
    public TopicResponse updateTopic(UUID topicId, UUID userId, TopicRequest request) {
        Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", topicId));

        topic.setName(request.name());
        topic.setDescription(request.description());

        if (request.parentId() != null) {
            if (request.parentId().equals(topicId)) {
                throw new BadRequestException("A topic cannot be its own parent");
            }
            Topic parent = topicRepository.findByIdAndUserId(request.parentId(), userId)
                    .orElseThrow(() -> new BadRequestException("Parent topic not found: " + request.parentId()));
            topic.setParent(parent);
        } else {
            topic.setParent(null);
        }

        topic = topicRepository.save(topic);
        return toResponseWithChildren(topic);
    }

    @Transactional
    public void deleteTopic(UUID topicId, UUID userId) {
        Topic topic = topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", topicId));
        topicRepository.delete(topic);
    }

    private TopicResponse toResponse(Topic topic) {
        long questionCount = topicRepository.countActiveQuestionsByTopicId(topic.getId());
        return new TopicResponse(
                topic.getId(),
                topic.getName(),
                topic.getParent() != null ? topic.getParent().getId() : null,
                topic.getDescription(),
                null,
                questionCount,
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }

    private TopicResponse toResponseWithChildren(Topic topic) {
        long questionCount = topicRepository.countActiveQuestionsByTopicId(topic.getId());
        List<TopicResponse> children = topic.getChildren() != null
                ? topic.getChildren().stream().map(this::toResponseWithChildren).collect(Collectors.toList())
                : null;

        return new TopicResponse(
                topic.getId(),
                topic.getName(),
                topic.getParent() != null ? topic.getParent().getId() : null,
                topic.getDescription(),
                children,
                questionCount,
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }
}
