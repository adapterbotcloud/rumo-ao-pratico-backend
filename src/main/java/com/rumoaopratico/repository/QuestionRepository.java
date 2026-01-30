package com.rumoaopratico.repository;

import com.rumoaopratico.model.Difficulty;
import com.rumoaopratico.model.Question;
import com.rumoaopratico.model.QuestionType;
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
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    Optional<Question> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT q FROM Question q WHERE q.user.id = :userId AND q.isActive = true " +
            "AND (:topicId IS NULL OR q.topic.id = :topicId) " +
            "AND (:type IS NULL OR q.type = :type) " +
            "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
            "AND (:search IS NULL OR LOWER(q.statement) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Question> findWithFilters(
            @Param("userId") UUID userId,
            @Param("topicId") UUID topicId,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("search") String search,
            Pageable pageable
    );

    long countByUserIdAndIsActiveTrue(UUID userId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.user.id = :userId AND q.isActive = true AND q.type = :type")
    long countByUserIdAndType(@Param("userId") UUID userId, @Param("type") QuestionType type);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.user.id = :userId AND q.isActive = true AND q.difficulty = :difficulty")
    long countByUserIdAndDifficulty(@Param("userId") UUID userId, @Param("difficulty") Difficulty difficulty);

    @Query(value = "SELECT q FROM Question q WHERE q.user.id = :userId AND q.isActive = true " +
            "AND (:topicIds IS NULL OR q.topic.id IN :topicIds) " +
            "AND (:types IS NULL OR q.type IN :types) " +
            "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
            "ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomQuestions(
            @Param("userId") UUID userId,
            @Param("topicIds") List<UUID> topicIds,
            @Param("types") List<QuestionType> types,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable
    );
}
