package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.QuestionOptionRequest;
import com.rumoaopratico.dto.request.QuestionRequest;
import com.rumoaopratico.dto.response.QuestionResponse;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.Question;
import com.rumoaopratico.model.QuestionOption;
import com.rumoaopratico.model.Topic;
import com.rumoaopratico.model.User;
import com.rumoaopratico.model.enums.Difficulty;
import com.rumoaopratico.model.enums.QuestionType;
import com.rumoaopratico.repository.QuestionRepository;
import com.rumoaopratico.repository.TopicRepository;
import com.rumoaopratico.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuestionService questionService;

    private User user;
    private Topic topic;
    private Question question;

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
                .explanation("Test explanation")
                .difficulty(Difficulty.MEDIUM)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .options(options)
                .build();

        QuestionOption opt1 = QuestionOption.builder().id(1L).question(question).text("Option A").isCorrect(true).build();
        QuestionOption opt2 = QuestionOption.builder().id(2L).question(question).text("Option B").isCorrect(false).build();
        options.add(opt1);
        options.add(opt2);
    }

    @Test
    void getQuestions_shouldReturnPage() {
        Page<Question> page = new PageImpl<>(List.of(question));
        when(questionRepository.findFiltered(eq(1L), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Page<QuestionResponse> result = questionService.getQuestions(1L, null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatement()).isEqualTo("Test question?");
    }

    @Test
    void getQuestion_shouldReturnQuestion() {
        when(questionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(question));

        QuestionResponse result = questionService.getQuestion(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatement()).isEqualTo("Test question?");
        assertThat(result.getOptions()).hasSize(2);
    }

    @Test
    void getQuestion_notFound_shouldThrow() {
        when(questionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.getQuestion(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createQuestion_shouldCreateSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        QuestionRequest request = QuestionRequest.builder()
                .topicId(1L)
                .type(QuestionType.MULTIPLE_CHOICE)
                .statement("Test question?")
                .explanation("Test explanation")
                .difficulty(Difficulty.MEDIUM)
                .options(List.of(
                        QuestionOptionRequest.builder().text("Option A").isCorrect(true).build(),
                        QuestionOptionRequest.builder().text("Option B").isCorrect(false).build()
                ))
                .build();

        QuestionResponse result = questionService.createQuestion(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatement()).isEqualTo("Test question?");
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void deleteQuestion_shouldSoftDelete() {
        when(questionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        questionService.deleteQuestion(1L, 1L);

        assertThat(question.getIsActive()).isFalse();
        verify(questionRepository).save(question);
    }

    @Test
    void updateQuestion_shouldUpdateSuccessfully() {
        when(questionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(question));
        when(topicRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(topic));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        QuestionRequest request = QuestionRequest.builder()
                .topicId(1L)
                .type(QuestionType.MULTIPLE_CHOICE)
                .statement("Updated question?")
                .difficulty(Difficulty.HARD)
                .options(List.of(
                        QuestionOptionRequest.builder().text("New A").isCorrect(true).build()
                ))
                .build();

        QuestionResponse result = questionService.updateQuestion(1L, 1L, request);

        assertThat(result).isNotNull();
        verify(questionRepository).save(any(Question.class));
    }
}
