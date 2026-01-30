package com.rumoaopratico.service;

import com.rumoaopratico.dto.question.*;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.*;
import com.rumoaopratico.repository.QuestionOptionRepository;
import com.rumoaopratico.repository.QuestionRepository;
import com.rumoaopratico.repository.TopicRepository;
import com.rumoaopratico.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuestionService questionService;

    private User testUser;
    private Topic testTopic;
    private Question testQuestion;
    private final UUID userId = UUID.randomUUID();
    private final UUID topicId = UUID.randomUUID();
    private final UUID questionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTopic = Topic.builder()
                .id(topicId)
                .user(testUser)
                .name("Test Topic")
                .description("A test topic")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        QuestionOption option1 = QuestionOption.builder()
                .id(UUID.randomUUID())
                .text("Option A")
                .isCorrect(true)
                .orderIndex(0)
                .build();

        QuestionOption option2 = QuestionOption.builder()
                .id(UUID.randomUUID())
                .text("Option B")
                .isCorrect(false)
                .orderIndex(1)
                .build();

        testQuestion = Question.builder()
                .id(questionId)
                .user(testUser)
                .topic(testTopic)
                .type(QuestionType.MULTIPLE_CHOICE)
                .statement("What is the answer?")
                .explanation("This is the explanation.")
                .difficulty(Difficulty.MEDIUM)
                .tags("test")
                .isActive(true)
                .options(new ArrayList<>(List.of(option1, option2)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        option1.setQuestion(testQuestion);
        option2.setQuestion(testQuestion);
    }

    @Test
    @DisplayName("Should create a question with options")
    void createQuestion_shouldCreateWithOptions() {
        QuestionRequest request = new QuestionRequest(
                topicId,
                QuestionType.MULTIPLE_CHOICE,
                "What is the answer?",
                "Explanation",
                "Bibliography",
                Difficulty.MEDIUM,
                "test",
                List.of(
                        new QuestionOptionRequest("Option A", true, null, 0),
                        new QuestionOptionRequest("Option B", false, null, 1)
                )
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(topicRepository.findByIdAndUserId(topicId, userId)).thenReturn(Optional.of(testTopic));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);

        QuestionResponse response = questionService.createQuestion(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.statement()).isEqualTo("What is the answer?");
        assertThat(response.type()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(response.options()).hasSize(2);

        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Should throw when creating question for non-existent topic")
    void createQuestion_shouldThrowWhenTopicNotFound() {
        QuestionRequest request = new QuestionRequest(
                topicId, QuestionType.MULTIPLE_CHOICE, "Q?", null, null, Difficulty.EASY, null, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(topicRepository.findByIdAndUserId(topicId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.createQuestion(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Topic");
    }

    @Test
    @DisplayName("Should list questions with pagination")
    void listQuestions_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Question> page = new PageImpl<>(List.of(testQuestion), pageable, 1);

        when(questionRepository.findWithFilters(userId, null, null, null, null, pageable)).thenReturn(page);

        Page<QuestionResponse> result = questionService.listQuestions(userId, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).statement()).isEqualTo("What is the answer?");
    }

    @Test
    @DisplayName("Should soft delete a question")
    void deleteQuestion_shouldSetInactive() {
        when(questionRepository.findByIdAndUserId(questionId, userId)).thenReturn(Optional.of(testQuestion));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);

        questionService.deleteQuestion(questionId, userId);

        assertThat(testQuestion.getIsActive()).isFalse();
        verify(questionRepository).save(testQuestion);
    }

    @Test
    @DisplayName("Should import questions from JSON format")
    void importQuestions_shouldParseAndSave() {
        QuestionImportRequest.ImportItem item = new QuestionImportRequest.ImportItem(
                "CF/88",
                "a) Option A<br>b) Option B<br>c) Option C",
                "a) Option A",
                null,
                null,
                "Qual é a resposta?",
                "Questão 1 :",
                null
        );
        QuestionImportRequest request = new QuestionImportRequest(topicId, List.of(item));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(topicRepository.findByIdAndUserId(topicId, userId)).thenReturn(Optional.of(testTopic));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question q = invocation.getArgument(0);
            q.setId(UUID.randomUUID());
            q.setCreatedAt(LocalDateTime.now());
            q.setUpdatedAt(LocalDateTime.now());
            return q;
        });

        List<QuestionResponse> result = questionService.importQuestions(userId, request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).statement()).isEqualTo("Qual é a resposta?");
        verify(questionRepository, times(1)).save(any(Question.class));
    }
}
