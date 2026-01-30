package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.TopicRequest;
import com.rumoaopratico.dto.response.TopicResponse;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.Topic;
import com.rumoaopratico.model.User;
import com.rumoaopratico.repository.TopicRepository;
import com.rumoaopratico.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public Page<TopicResponse> getTopics(Long userId, Pageable pageable) {
        // Topics are global — all users see all topics
        return topicRepository.findAll(pageable)
                .map(TopicResponse::from);
    }

    public TopicResponse getTopic(Long userId, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", topicId));
        return TopicResponse.from(topic);
    }

    @Transactional
    public TopicResponse createTopic(Long userId, TopicRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Topic parent = null;
        if (request.getParentId() != null) {
            parent = topicRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent topic", request.getParentId()));
        }

        Topic topic = Topic.builder()
                .user(user)
                .name(request.getName())
                .parent(parent)
                .build();

        topic = topicRepository.save(topic);
        return TopicResponse.from(topic);
    }

    @Transactional
    public TopicResponse updateTopic(Long userId, Long topicId, TopicRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", topicId));

        topic.setName(request.getName());

        if (request.getParentId() != null) {
            Topic parent = topicRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent topic", request.getParentId()));
            topic.setParent(parent);
        } else {
            topic.setParent(null);
        }

        topic = topicRepository.save(topic);
        return TopicResponse.from(topic);
    }

    @Transactional
    public void deleteTopic(Long userId, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", topicId));
        topicRepository.delete(topic);
    }
}
