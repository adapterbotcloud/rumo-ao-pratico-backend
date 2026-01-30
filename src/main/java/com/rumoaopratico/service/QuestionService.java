package com.rumoaopratico.service;

import com.rumoaopratico.dto.question.*;
import com.rumoaopratico.exception.ResourceNotFoundException;
import com.rumoaopratico.model.*;
import com.rumoaopratico.repository.QuestionOptionRepository;
import com.rumoaopratico.repository.QuestionRepository;
import com.rumoaopratico.repository.TopicRepository;
import com.rumoaopratico.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public QuestionService(QuestionRepository questionRepository, QuestionOptionRepository questionOptionRepository,
                           TopicRepository topicRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    public Page<QuestionResponse> listQuestions(UUID userId, UUID topicId, QuestionType type,
                                                 Difficulty difficulty, String search, Pageable pageable) {
        return questionRepository.findWithFilters(userId, topicId, type, difficulty, search, pageable)
                .map(this::toResponse);
    }

    public QuestionResponse getQuestionById(UUID questionId, UUID userId) {
        Question question = questionRepository.findByIdAndUserId(questionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));
        return toResponse(question);
    }

    @Transactional
    public QuestionResponse createQuestion(UUID userId, QuestionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Topic topic = topicRepository.findByIdAndUserId(request.topicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.topicId()));

        Question question = Question.builder()
                .user(user)
                .topic(topic)
                .type(request.type())
                .statement(request.statement())
                .explanation(request.explanation())
                .bibliography(request.bibliography())
                .difficulty(request.difficulty())
                .tags(request.tags())
                .isActive(true)
                .build();

        if (request.options() != null) {
            for (int i = 0; i < request.options().size(); i++) {
                QuestionOptionRequest optReq = request.options().get(i);
                QuestionOption option = QuestionOption.builder()
                        .text(optReq.text())
                        .isCorrect(optReq.isCorrect())
                        .explanation(optReq.explanation())
                        .orderIndex(optReq.orderIndex() != null ? optReq.orderIndex() : i)
                        .build();
                question.addOption(option);
            }
        }

        question = questionRepository.save(question);
        return toResponse(question);
    }

    @Transactional
    public QuestionResponse updateQuestion(UUID questionId, UUID userId, QuestionRequest request) {
        Question question = questionRepository.findByIdAndUserId(questionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        Topic topic = topicRepository.findByIdAndUserId(request.topicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.topicId()));

        question.setTopic(topic);
        question.setType(request.type());
        question.setStatement(request.statement());
        question.setExplanation(request.explanation());
        question.setBibliography(request.bibliography());
        question.setDifficulty(request.difficulty());
        question.setTags(request.tags());

        if (request.options() != null) {
            question.clearOptions();
            questionRepository.flush();
            for (int i = 0; i < request.options().size(); i++) {
                QuestionOptionRequest optReq = request.options().get(i);
                QuestionOption option = QuestionOption.builder()
                        .text(optReq.text())
                        .isCorrect(optReq.isCorrect())
                        .explanation(optReq.explanation())
                        .orderIndex(optReq.orderIndex() != null ? optReq.orderIndex() : i)
                        .build();
                question.addOption(option);
            }
        }

        question = questionRepository.save(question);
        return toResponse(question);
    }

    @Transactional
    public void deleteQuestion(UUID questionId, UUID userId) {
        Question question = questionRepository.findByIdAndUserId(questionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));
        question.setIsActive(false);
        questionRepository.save(question);
    }

    @Transactional
    public List<QuestionResponse> importQuestions(UUID userId, QuestionImportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Topic topic = topicRepository.findByIdAndUserId(request.topicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.topicId()));

        List<QuestionResponse> imported = new ArrayList<>();

        for (QuestionImportRequest.ImportItem item : request.results()) {
            try {
                String statement = item.pergunta() != null ? item.pergunta() : item.question();
                if (statement == null || statement.isBlank()) {
                    log.warn("Skipping import item with no question text");
                    continue;
                }

                Question question = Question.builder()
                        .user(user)
                        .topic(topic)
                        .type(QuestionType.MULTIPLE_CHOICE)
                        .statement(statement)
                        .bibliography(item.bibliografia())
                        .difficulty(Difficulty.MEDIUM)
                        .isActive(true)
                        .build();

                // Parse options from "Items" field (HTML format: "a) ...<br>b) ...")
                if (item.items() != null && !item.items().isBlank()) {
                    List<String> optionTexts = parseItemsHtml(item.items());
                    String correctText = item.correct() != null ? item.correct().trim() : "";

                    for (int i = 0; i < optionTexts.size(); i++) {
                        String optText = optionTexts.get(i).trim();
                        boolean isCorrect = !correctText.isEmpty() && normalizeOption(optText).equals(normalizeOption(correctText));

                        QuestionOption option = QuestionOption.builder()
                                .text(optText)
                                .isCorrect(isCorrect)
                                .orderIndex(i)
                                .build();
                        question.addOption(option);
                    }
                } else if (item.correctAnswer() != null && item.incorrectAnswers() != null) {
                    // Alternative format with correct_answer and incorrect_answers
                    List<String> allAnswers = new ArrayList<>(item.incorrectAnswers());
                    allAnswers.add(item.correctAnswer());
                    Collections.shuffle(allAnswers);

                    for (int i = 0; i < allAnswers.size(); i++) {
                        String optText = allAnswers.get(i);
                        QuestionOption option = QuestionOption.builder()
                                .text(optText)
                                .isCorrect(optText.equals(item.correctAnswer()))
                                .orderIndex(i)
                                .build();
                        question.addOption(option);
                    }
                }

                question = questionRepository.save(question);
                imported.add(toResponse(question));
            } catch (Exception e) {
                log.error("Failed to import question: {}", e.getMessage());
            }
        }

        log.info("Imported {} questions for user {} into topic {}", imported.size(), userId, request.topicId());
        return imported;
    }

    private List<String> parseItemsHtml(String items) {
        // Split by <br>, <br/>, or <br /> tags
        String[] parts = items.split("<br\\s*/?>|\\n");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String cleaned = part.replaceAll("<[^>]+>", "").trim();
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return result;
    }

    private String normalizeOption(String text) {
        return text.replaceAll("^[a-eA-E]\\)\\s*", "").trim().toLowerCase();
    }

    public QuestionResponse toResponse(Question question) {
        List<QuestionOptionResponse> optionResponses = question.getOptions().stream()
                .map(opt -> new QuestionOptionResponse(
                        opt.getId(),
                        opt.getText(),
                        opt.getIsCorrect(),
                        opt.getExplanation(),
                        opt.getOrderIndex()
                ))
                .collect(Collectors.toList());

        return new QuestionResponse(
                question.getId(),
                question.getTopic().getId(),
                question.getTopic().getName(),
                question.getType(),
                question.getStatement(),
                question.getExplanation(),
                question.getBibliography(),
                question.getDifficulty(),
                question.getTags(),
                question.getIsActive(),
                optionResponses,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
