package com.rumoaopratico.repository;

import com.rumoaopratico.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    Page<Topic> findByUserIdAndParentIsNull(UUID userId, Pageable pageable);

    List<Topic> findByUserIdAndParentIsNull(UUID userId);

    List<Topic> findByUserId(UUID userId);

    Optional<Topic> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.topic.id = :topicId AND q.isActive = true")
    long countActiveQuestionsByTopicId(@Param("topicId") UUID topicId);
}
