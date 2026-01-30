package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.QuizAnswerRequest;
import com.rumoaopratico.dto.request.QuizStartRequest;
import com.rumoaopratico.dto.response.QuizAnswerResponse;
import com.rumoaopratico.dto.response.QuizAttemptResponse;
import com.rumoaopratico.dto.response.QuizResultResponse;
import com.rumoaopratico.exception.BadRequestException;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.*;
import com.rumoaopratico.model.enums.Difficulty;
import com.rumoaopratico.model.enums.QuestionType;
import com.rumoaopratico.model.enums.QuizMode;
import com.rumoaopratico.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private QuizAnswerRepository quizAnswerRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizService quizService;

    private User user;
    private Topic topic;
    private Question question;
    private QuizAttempt attempt;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").email("test@test.com").build();
        topic = Topic.builder().id(1L).user(user).name("Arte Naval").build();

        List<QuestionOption> options = new ArrayList<>();
        question = Question.builder()
                .id(1L)
                .user(user)
                .topic(topic)
                .type(QuestionType.MULTIPLE_CHOICE)
                .statement("Test question?")
                .isActive(true)
                .options(options)
                .build();

        QuestionOption opt1 = QuestionOption.builder()
                .id(10L).question(question).text("Correct").isCorrect(true).build();
        QuestionOption opt2 = QuestionOption.builder()
                .id(11L).question(question).text("Wrong").isCorrect(false).build();
        options.add(opt1);
        options.add(opt2);

        Map<String, Object> config = new HashMap<>();
        config.put("questionIds", List.of(1L));
        config.put("topicIds", List.of(1L));

        attempt = QuizAttempt.builder()
                .id(1L)
                .user(user)
                .totalQuestions(1)
                .correctCount(0)
                .mode(QuizMode.STUDY)
                .configJson(config)
                .startedAt(LocalDateTime.now())
                .answers(new ArrayList<>())
                .build();
    }

    @Test
    void startQuiz_shouldCreateAttempt() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(questionRepository.findRandomForQuiz(eq(1L), eq(List.of(1L)), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(List.of(question));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(attempt);

        QuizStartRequest request = QuizStartRequest.builder()
                .topicIds(List.of(1L))
                .questionCount(10)
                .mode(QuizMode.STUDY)
                .build();

        QuizAttemptResponse result = quizService.startQuiz(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getMode()).isEqualTo(QuizMode.STUDY);
        assertThat(result.getQuestions()).hasSize(1);
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void startQuiz_noQuestions_shouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(questionRepository.findRandomForQuiz(eq(1L), eq(List.of(99L)), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(List.of());

        QuizStartRequest request = QuizStartRequest.builder()
                .topicIds(List.of(99L))
                .questionCount(10)
                .mode(QuizMode.EVALUATION)
                .build();

        assertThatThrownBy(() -> quizService.startQuiz(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No questions found");
    }

    @Test
    void submitAnswer_correctAnswer_shouldReturnTrue() {
        when(quizAttemptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(attempt));
        when(quizAnswerRepository.existsByAttemptIdAndQuestionId(1L, 1L)).thenReturn(false);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        QuizAnswer savedAnswer = QuizAnswer.builder()
                .id(1L)
                .attempt(attempt)
                .question(question)
                .userAnswerJson(Map.of("selectedOptionId", 10L))
                .isCorrect(true)
                .answeredAt(LocalDateTime.now())
                .build();
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(savedAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(attempt);

        QuizAnswerRequest request = QuizAnswerRequest.builder()
                .questionId(1L)
                .answer(Map.of("selectedOptionId", 10))
                .build();

        QuizAnswerResponse result = quizService.submitAnswer(1L, 1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isTrue();
    }

    @Test
    void submitAnswer_alreadyAnswered_shouldThrow() {
        when(quizAttemptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(attempt));
        when(quizAnswerRepository.existsByAttemptIdAndQuestionId(1L, 1L)).thenReturn(true);

        QuizAnswerRequest request = QuizAnswerRequest.builder()
                .questionId(1L)
                .answer(Map.of("selectedOptionId", 10))
                .build();

        assertThatThrownBy(() -> quizService.submitAnswer(1L, 1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already answered");
    }

    @Test
    void finishQuiz_shouldReturnResult() {
        when(quizAttemptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(attempt));
        when(quizAnswerRepository.countByAttemptId(1L)).thenReturn(1L);
        when(quizAnswerRepository.countByAttemptIdAndIsCorrectTrue(1L)).thenReturn(1L);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(attempt);

        QuizAnswer answer = QuizAnswer.builder()
                .id(1L).attempt(attempt).question(question)
                .userAnswerJson(Map.of("selectedOptionId", 10))
                .isCorrect(true).answeredAt(LocalDateTime.now())
                .build();
        when(quizAnswerRepository.findByAttemptId(1L)).thenReturn(List.of(answer));

        QuizResultResponse result = quizService.finishQuiz(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getCorrectCount()).isEqualTo(1);
        assertThat(result.getSuccessRate()).isEqualTo(100.0);
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void finishQuiz_alreadyFinished_shouldThrow() {
        attempt.setFinishedAt(LocalDateTime.now());
        when(quizAttemptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> quizService.finishQuiz(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already finished");
    }
}
