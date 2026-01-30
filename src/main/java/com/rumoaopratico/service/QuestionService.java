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
import com.rumoaopratico.repository.QuizAnswerRepository;
import com.rumoaopratico.repository.TopicRepository;
import com.rumoaopratico.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public Page<QuestionResponse> getQuestions(Long userId, Long topicId, QuestionType type,
                                               Difficulty difficulty, String search,
                                               String answerStatus, Pageable pageable) {
        // Select the right query based on answerStatus filter
        Page<Question> questionsPage;
        if (answerStatus != null && userId != null) {
            switch (answerStatus.toUpperCase()) {
                case "UNANSWERED":
                    questionsPage = questionRepository.findFilteredGlobalUnanswered(
                            userId, topicId, type, difficulty, search, pageable);
                    break;
                case "CORRECT":
                    questionsPage = questionRepository.findFilteredGlobalCorrect(
                            userId, topicId, type, difficulty, search, pageable);
                    break;
                case "INCORRECT":
                    questionsPage = questionRepository.findFilteredGlobalIncorrect(
                            userId, topicId, type, difficulty, search, pageable);
                    break;
                default:
                    questionsPage = questionRepository.findFilteredGlobal(
                            topicId, type, difficulty, search, pageable);
            }
        } else {
            questionsPage = questionRepository.findFilteredGlobal(
                    topicId, type, difficulty, search, pageable);
        }

        Page<QuestionResponse> responsePage = questionsPage.map(QuestionResponse::from);

        // Enrich with answer stats
        if (userId != null) {
            List<QuestionResponse> content = responsePage.getContent();
            List<Long> questionIds = content.stream()
                    .map(QuestionResponse::getId)
                    .collect(Collectors.toList());

            if (!questionIds.isEmpty()) {
                List<Object[]> stats = quizAnswerRepository.findAnswerStatsByUserAndQuestions(userId, questionIds);
                Map<Long, Object[]> statsMap = new HashMap<>();
                for (Object[] row : stats) {
                    Long qId = ((Number) row[0]).longValue();
                    statsMap.put(qId, row);
                }

                for (QuestionResponse qr : content) {
                    Object[] stat = statsMap.get(qr.getId());
                    if (stat != null) {
                        int answered = ((Number) stat[1]).intValue();
                        int correct = ((Number) stat[2]).intValue();
                        qr.setTimesAnswered(answered);
                        qr.setTimesCorrect(correct);
                        qr.setAnswerStatus(correct > 0 ? "CORRECT" : "INCORRECT");
                    } else {
                        qr.setTimesAnswered(0);
                        qr.setTimesCorrect(0);
                        qr.setAnswerStatus("UNANSWERED");
                    }
                }
            }
        }

        return responsePage;
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long userId, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .filter(q -> Boolean.TRUE.equals(q.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        return QuestionResponse.from(question);
    }

    @Transactional
    public QuestionResponse createQuestion(Long userId, QuestionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Topic topic = topicRepository.findById(request.getTopicId())
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
        Question question = questionRepository.findById(questionId)
                .filter(q -> Boolean.TRUE.equals(q.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));

        Topic topic = topicRepository.findById(request.getTopicId())
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
        Question question = questionRepository.findById(questionId)
                .filter(q -> Boolean.TRUE.equals(q.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        // Soft delete
        question.setIsActive(false);
        questionRepository.save(question);
    }
}
