package com.rumoaopratico.service;

import com.rumoaopratico.dto.response.HistoryEntryResponse;
import com.rumoaopratico.dto.response.QuizResultResponse;
import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.model.Topic;
import com.rumoaopratico.repository.QuizAttemptRepository;
import com.rumoaopratico.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizService quizService;
    private final TopicRepository topicRepository;

    @Transactional(readOnly = true)
    public Page<HistoryEntryResponse> getHistory(Long userId, LocalDateTime startDate,
                                                  LocalDateTime endDate, Pageable pageable) {
        Page<QuizAttempt> attempts = quizAttemptRepository.findByUserIdFiltered(userId, startDate, endDate, pageable);

        // Collect all topic IDs from all attempts' configJson
        Set<Long> allTopicIds = new HashSet<>();
        for (QuizAttempt attempt : attempts.getContent()) {
            allTopicIds.addAll(extractTopicIds(attempt));
        }

        // Batch-load topic names
        Map<Long, String> topicNameMap = allTopicIds.isEmpty()
                ? Collections.emptyMap()
                : topicRepository.findAllById(allTopicIds).stream()
                    .collect(Collectors.toMap(Topic::getId, Topic::getName));

        return attempts.map(attempt -> {
            List<String> topicNames = extractTopicIds(attempt).stream()
                    .map(topicNameMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return HistoryEntryResponse.from(attempt, topicNames);
        });
    }

    @Transactional(readOnly = true)
    public QuizResultResponse getHistoryDetail(Long userId, Long attemptId) {
        return quizService.getResult(userId, attemptId);
    }

    @SuppressWarnings("unchecked")
    private List<Long> extractTopicIds(QuizAttempt attempt) {
        if (attempt.getConfigJson() != null && attempt.getConfigJson().containsKey("topicIds")) {
            Object raw = attempt.getConfigJson().get("topicIds");
            if (raw instanceof List) {
                return ((List<Number>) raw).stream()
                        .map(Number::longValue)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
