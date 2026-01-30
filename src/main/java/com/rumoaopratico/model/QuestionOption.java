package com.rumoaopratico.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "question")
@EqualsAndHashCode(exclude = "question")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "is_correct")
    @Builder.Default
    private Boolean isCorrect = false;

    @Column(columnDefinition = "TEXT")
    private String explanation;
}
