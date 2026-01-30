package com.rumoaopratico.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"options", "topic", "user"})
@ToString(exclude = {"options", "topic", "user"})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "question_type")
    private QuestionType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String statement;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String bibliography;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "difficulty_level")
    private Difficulty difficulty;

    @Column
    private String tags;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void clearOptions() {
        options.forEach(o -> o.setQuestion(null));
        options.clear();
    }
}
