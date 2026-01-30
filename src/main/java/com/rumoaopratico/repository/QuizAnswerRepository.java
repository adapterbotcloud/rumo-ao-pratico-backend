package com.rumoaopratico.repository;

import com.rumoaopratico.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByAttemptId(Long attemptId);
    long countByAttemptId(Long attemptId);
    long countByAttemptIdAndIsCorrectTrue(Long attemptId);
    boolean existsByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    @Modifying
    @Query("DELETE FROM QuizAnswer qa WHERE qa.attempt.user.id = :userId")
    void deleteByAttemptUserId(@Param("userId") Long userId);
}
