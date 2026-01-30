package com.rumoaopratico.repository;

import com.rumoaopratico.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    // Global queries (topics are shared)
    Page<Topic> findAll(Pageable pageable);
    Optional<Topic> findByName(String name);

    // Legacy per-user queries (kept for backward compatibility)
    Page<Topic> findByUserId(Long userId, Pageable pageable);
    List<Topic> findByUserId(Long userId);
    Optional<Topic> findByIdAndUserId(Long id, Long userId);
    Optional<Topic> findByNameAndUserId(String name, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
}
