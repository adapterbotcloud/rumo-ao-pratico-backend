package com.rumoaopratico.repository;

import com.rumoaopratico.model.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR qa.startedAt >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR qa.startedAt <= :endDate) " +
           "ORDER BY qa.startedAt DESC")
    Page<QuizAttempt> findByUserIdFiltered(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByUserId(Long userId);

    List<QuizAttempt> findAllByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(qa.correctCount), 0) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.finishedAt IS NOT NULL")
    long sumCorrectCountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(qa.totalQuestions), 0) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.finishedAt IS NOT NULL")
    long sumTotalQuestionsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM QuizAttempt qa WHERE qa.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
