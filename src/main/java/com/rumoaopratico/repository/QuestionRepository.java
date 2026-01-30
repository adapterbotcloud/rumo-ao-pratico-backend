package com.rumoaopratico.repository;

import com.rumoaopratico.model.Question;
import com.rumoaopratico.model.enums.Difficulty;
import com.rumoaopratico.model.enums.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByIdAndUserId(Long id, Long userId);

    // Global queries (questions are shared across users)
    @Query("SELECT q FROM Question q WHERE q.isActive = true " +
           "AND (:topicId IS NULL OR q.topic.id = :topicId) " +
           "AND (:type IS NULL OR q.type = :type) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:search IS NULL OR LOWER(CAST(q.statement AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Question> findFilteredGlobal(
            @Param("topicId") Long topicId,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.isActive = true " +
           "AND q.topic.id IN :topicIds " +
           "AND (:type IS NULL OR q.type = :type) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomForQuizGlobal(
            @Param("topicIds") List<Long> topicIds,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.isActive = true")
    long countAllActive();

    // Legacy per-user queries
    @Query("SELECT q FROM Question q WHERE q.user.id = :userId AND q.isActive = true " +
           "AND (:topicId IS NULL OR q.topic.id = :topicId) " +
           "AND (:type IS NULL OR q.type = :type) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:search IS NULL OR LOWER(CAST(q.statement AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Question> findFiltered(
            @Param("userId") Long userId,
            @Param("topicId") Long topicId,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.user.id = :userId AND q.isActive = true " +
           "AND q.topic.id IN :topicIds " +
           "AND (:type IS NULL OR q.type = :type) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomForQuiz(
            @Param("userId") Long userId,
            @Param("topicIds") List<Long> topicIds,
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable);

    long countByUserIdAndIsActiveTrue(Long userId);

    long countByUserIdAndTopicIdAndIsActiveTrue(Long userId, Long topicId);
}
