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

    @Query("SELECT COUNT(DISTINCT qa.question.id) FROM QuizAnswer qa WHERE qa.attempt.user.id = :userId")
    long countUniqueQuestionsAnsweredByUserId(@Param("userId") Long userId);

    @Query("SELECT qa.question.id, " +
           "COUNT(qa), " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) " +
           "FROM QuizAnswer qa WHERE qa.attempt.user.id = :userId " +
           "AND qa.question.id IN :questionIds " +
           "GROUP BY qa.question.id")
    List<Object[]> findAnswerStatsByUserAndQuestions(
            @Param("userId") Long userId,
            @Param("questionIds") List<Long> questionIds);
}
