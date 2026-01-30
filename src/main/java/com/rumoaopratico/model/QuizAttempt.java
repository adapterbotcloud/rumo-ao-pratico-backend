package com.rumoaopratico.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"answers", "user"})
@ToString(exclude = {"answers", "user"})
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "correct_count", nullable = false)
    @Builder.Default
    private Integer correctCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "quiz_mode")
    private QuizMode mode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_json", columnDefinition = "jsonb")
    private Map<String, Object> configJson;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuizAnswer> answers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
