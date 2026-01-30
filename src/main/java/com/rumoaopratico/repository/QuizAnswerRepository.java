package com.rumoaopratico.repository;

import com.rumoaopratico.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByAttemptId(Long attemptId);
    long countByAttemptId(Long attemptId);
    long countByAttemptIdAndIsCorrectTrue(Long attemptId);
    boolean existsByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
