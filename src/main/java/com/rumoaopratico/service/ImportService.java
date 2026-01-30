package com.rumoaopratico.service;

import com.rumoaopratico.dto.request.ImportQuestionsRequest;
import com.rumoaopratico.dto.request.ImportQuestionsRequest.ImportQuestionItem;
import com.rumoaopratico.dto.response.ImportResultResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    private static final Pattern OPTION_LETTER_PATTERN = Pattern.compile("^([a-e])\\)\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CORRECT_LETTER_PATTERN = Pattern.compile("^([a-e])\\)", Pattern.CASE_INSENSITIVE);

    @Transactional
    public ImportResultResponse importQuestions(Long userId, ImportQuestionsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Find or create topic
        String topicName = request.getTopicName();
        Topic topic = topicRepository.findByNameAndUserId(topicName, userId)
                .orElseGet(() -> {
                    Topic newTopic = Topic.builder()
                            .user(user)
                            .name(topicName)
                            .build();
                    return topicRepository.save(newTopic);
                });

        List<String> errors = new ArrayList<>();
        int imported = 0;

        for (int i = 0; i < request.getResults().size(); i++) {
            ImportQuestionItem item = request.getResults().get(i);
            try {
                Question question = parseAndCreateQuestion(user, topic, item);
                questionRepository.save(question);
                imported++;
            } catch (Exception e) {
                String errorMsg = String.format("Error importing question %d: %s", i + 1, e.getMessage());
                errors.add(errorMsg);
                log.warn(errorMsg, e);
            }
        }

        log.info("Import complete for topic '{}': {}/{} imported, {} errors",
                topicName, imported, request.getResults().size(), errors.size());

        return ImportResultResponse.builder()
                .totalProcessed(request.getResults().size())
                .totalImported(imported)
                .totalErrors(errors.size())
                .errors(errors)
                .topicName(topicName)
                .topicId(topic.getId())
                .build();
    }

    private Question parseAndCreateQuestion(User user, Topic topic, ImportQuestionItem item) {
        // Use pergunta (Portuguese) first, then question (English)
        String statement = StringUtils.hasText(item.getPergunta()) ? item.getPergunta() : item.getQuestion();
        if (!StringUtils.hasText(statement)) {
            throw new IllegalArgumentException("Question statement is empty");
        }

        String bibliography = item.getBibliografia();

        Question question = Question.builder()
                .user(user)
                .topic(topic)
                .type(QuestionType.MULTIPLE_CHOICE)
                .statement(statement.trim())
                .bibliography(bibliography)
                .difficulty(Difficulty.MEDIUM)
                .isActive(true)
                .options(new ArrayList<>())
                .build();

        // Parse options from Items field (format: "a) opt1<br>b) opt2<br>...")
        if (StringUtils.hasText(item.getItems())) {
            parseOptionsFromItems(question, item);
        } else if (item.getCorrect_answer() != null && item.getIncorrect_answers() != null) {
            // Fallback: use correct_answer and incorrect_answers fields
            parseOptionsFromArrays(question, item);
        }

        return question;
    }

    private void parseOptionsFromItems(Question question, ImportQuestionItem item) {
        String[] rawOptions = item.getItems().split("<br>");
        String correctLetter = null;

        if (StringUtils.hasText(item.getCorrect())) {
            Matcher matcher = CORRECT_LETTER_PATTERN.matcher(item.getCorrect().trim());
            if (matcher.find()) {
                correctLetter = matcher.group(1).toLowerCase();
            }
        }

        for (String rawOpt : rawOptions) {
            String trimmed = rawOpt.trim();
            if (trimmed.isEmpty()) continue;

            Matcher matcher = OPTION_LETTER_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                String letter = matcher.group(1).toLowerCase();
                String text = matcher.group(2).trim();
                boolean isCorrect = letter.equals(correctLetter);

                QuestionOption option = QuestionOption.builder()
                        .text(text)
                        .isCorrect(isCorrect)
                        .build();
                question.addOption(option);
            } else {
                // Option without letter prefix
                QuestionOption option = QuestionOption.builder()
                        .text(trimmed)
                        .isCorrect(false)
                        .build();
                question.addOption(option);
            }
        }
    }

    private void parseOptionsFromArrays(Question question, ImportQuestionItem item) {
        // Add correct answer
        QuestionOption correctOption = QuestionOption.builder()
                .text(item.getCorrect_answer().trim())
                .isCorrect(true)
                .build();
        question.addOption(correctOption);

        // Add incorrect answers
        for (String incorrect : item.getIncorrect_answers()) {
            QuestionOption option = QuestionOption.builder()
                    .text(incorrect.trim())
                    .isCorrect(false)
                    .build();
            question.addOption(option);
        }
    }
}
