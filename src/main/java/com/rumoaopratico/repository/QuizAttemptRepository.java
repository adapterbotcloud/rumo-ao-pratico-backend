package com.rumoaopratico.repository;

import com.rumoaopratico.model.QuizAttempt;
import com.rumoaopratico.model.QuizMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    Optional<QuizAttempt> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT a FROM QuizAttempt a WHERE a.user.id = :userId " +
            "AND (:mode IS NULL OR a.mode = :mode) " +
            "ORDER BY a.createdAt DESC")
    Page<QuizAttempt> findByUserIdWithFilters(
            @Param("userId") UUID userId,
            @Param("mode") QuizMode mode,
            Pageable pageable
    );

    long countByUserId(UUID userId);

    @Query("SELECT COALESCE(SUM(a.correctCount), 0) FROM QuizAttempt a WHERE a.user.id = :userId AND a.finishedAt IS NOT NULL")
    long sumCorrectCountByUserId(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(a.totalQuestions), 0) FROM QuizAttempt a WHERE a.user.id = :userId AND a.finishedAt IS NOT NULL")
    long sumTotalQuestionsByUserId(@Param("userId") UUID userId);
}
