-- V2: Create indexes for performance

CREATE INDEX idx_topics_user_id ON topics(user_id);
CREATE INDEX idx_questions_user_id ON questions(user_id);
CREATE INDEX idx_questions_topic_id ON questions(topic_id);
CREATE INDEX idx_questions_created_at ON questions(created_at);
CREATE INDEX idx_quiz_attempts_user_id ON quiz_attempts(user_id);
CREATE INDEX idx_quiz_attempts_created_at ON quiz_attempts(created_at);
CREATE INDEX idx_quiz_answers_attempt_id ON quiz_answers(attempt_id);
