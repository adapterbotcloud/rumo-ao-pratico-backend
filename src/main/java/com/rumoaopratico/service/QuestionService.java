package com.rumoaopratico.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public Page<QuestionResponse> getQuestions(Long userId, Long topicId, QuestionType type,
                                               Difficulty difficulty, String search, Pageable pageable) {
        return questionRepository.findFiltered(userId, topicId, type, difficulty, search, pageable)
                .map(QuestionResponse::from);
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long userId, Long questionId) {
        Question question = questionRepository.findByIdAndUserId(questionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        return QuestionResponse.from(question);
    }

    @Transactional
    public QuestionResponse createQuestion(Long userId, QuestionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Topic topic = topicRepository.findByIdAndUserId(request.getTopicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", request.getTopicId()));

        Question question = Question.builder()
                .user(user)
                .topic(topic)
                .type(request.getType())
                .statement(request.getStatement())
                .explanation(request.getExplanation())
                .bibliography(request.getBibliography())
                .difficulty(request.getDifficulty())
                .tags(request.getTags())
                .isActive(true)
                .options(new ArrayList<>())
                .build();

        if (request.getOptions() != null) {
            request.getOptions().forEach(optReq -> {
                QuestionOption option = QuestionOption.builder()
                        .text(optReq.getText())
                        .isCorrect(optReq.getIsCorrect() != null ? optReq.getIsCorrect() : false)
                        .explanation(optReq.getExplanation())
                        .build();
                question.addOption(option);
            });
        }

        Question saved = questionRepository.save(question);
        return QuestionResponse.from(saved);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long userId, Long questionId, QuestionRequest request) {
        Question question = questionRepository.findByIdAndUserId(questionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));

        Topic topic = topicRepository.findByIdAndUserId(request.getTopicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", request.getTopicId()));

        question.setTopic(topic);
        question.setType(request.getType());
        question.setStatement(request.getStatement());
        question.setExplanation(request.getExplanation());
        question.setBibliography(request.getBibliography());
        question.setDifficulty(request.getDifficulty());
        question.setTags(request.getTags());

        // Update options
        question.getOptions().clear();
        if (request.getOptions() != null) {
            request.getOptions().forEach(optReq -> {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .text(optReq.getText())
                        .isCorrect(optReq.getIsCorrect() != null ? optReq.getIsCorrect() : false)
                        .explanation(optReq.getExplanation())
                        .build();
                question.getOptions().add(option);
            });
        }

        Question saved = questionRepository.save(question);
        return QuestionResponse.from(saved);
    }

    @Transactional
    public void deleteQuestion(Long userId, Long questionId) {
        Question question = questionRepository.findByIdAndUserId(questionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        // Soft delete
        question.setIsActive(false);
        questionRepository.save(question);
    }
}
