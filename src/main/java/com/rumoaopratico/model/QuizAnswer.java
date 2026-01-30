package com.rumoaopratico.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "quiz_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"attempt", "question"})
@ToString(exclude = {"attempt", "question"})
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_answer_json", columnDefinition = "jsonb")
    private Map<String, Object> userAnswerJson;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    @Column(name = "answered_at", nullable = false)
    @Builder.Default
    private LocalDateTime answeredAt = LocalDateTime.now();
}
