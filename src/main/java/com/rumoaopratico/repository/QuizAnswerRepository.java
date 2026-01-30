package com.rumoaopratico.repository;

import com.rumoaopratico.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, UUID> {

    List<QuizAnswer> findByAttemptId(UUID attemptId);

    long countByAttemptId(UUID attemptId);

    @Query("SELECT COUNT(a) FROM QuizAnswer a WHERE a.attempt.id = :attemptId AND a.isCorrect = true")
    long countCorrectByAttemptId(@Param("attemptId") UUID attemptId);

    @Query("SELECT COUNT(a) FROM QuizAnswer a WHERE a.attempt.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(a) FROM QuizAnswer a WHERE a.attempt.user.id = :userId AND a.isCorrect = true")
    long countCorrectByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(a) FROM QuizAnswer a WHERE a.attempt.user.id = :userId AND a.question.topic.id = :topicId")
    long countByUserIdAndTopicId(@Param("userId") UUID userId, @Param("topicId") UUID topicId);

    @Query("SELECT COUNT(a) FROM QuizAnswer a WHERE a.attempt.user.id = :userId AND a.question.topic.id = :topicId AND a.isCorrect = true")
    long countCorrectByUserIdAndTopicId(@Param("userId") UUID userId, @Param("topicId") UUID topicId);

    boolean existsByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
}
