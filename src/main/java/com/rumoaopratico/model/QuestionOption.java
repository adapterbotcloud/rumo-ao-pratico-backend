package com.rumoaopratico.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "question_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"question"})
@ToString(exclude = {"question"})
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;
}
