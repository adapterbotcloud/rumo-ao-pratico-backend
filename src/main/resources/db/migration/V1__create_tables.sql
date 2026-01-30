-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Enum types
CREATE TYPE question_type AS ENUM ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'FLASHCARD', 'COMMENTED_PHRASE');
CREATE TYPE difficulty_level AS ENUM ('EASY', 'MEDIUM', 'HARD');
CREATE TYPE quiz_mode AS ENUM ('EVALUATION', 'STUDY');

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- Topics table (self-referencing for hierarchy)
CREATE TABLE topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    parent_id UUID REFERENCES topics(id) ON DELETE SET NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_topics_user_id ON topics(user_id);
CREATE INDEX idx_topics_parent_id ON topics(parent_id);

-- Questions table
CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    type question_type NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    statement TEXT NOT NULL,
    explanation TEXT,
    bibliography TEXT,
    difficulty difficulty_level NOT NULL DEFAULT 'MEDIUM',
    tags TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_questions_user_id ON questions(user_id);
CREATE INDEX idx_questions_topic_id ON questions(topic_id);
CREATE INDEX idx_questions_type ON questions(type);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
CREATE INDEX idx_questions_is_active ON questions(is_active);

-- Question Options table
CREATE TABLE question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    explanation TEXT,
    order_index INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_question_options_question_id ON question_options(question_id);

-- Quiz Attempts table
CREATE TABLE quiz_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    finished_at TIMESTAMP,
    total_questions INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    mode quiz_mode NOT NULL DEFAULT 'STUDY',
    config_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_attempts_user_id ON quiz_attempts(user_id);
CREATE INDEX idx_quiz_attempts_mode ON quiz_attempts(mode);

-- Quiz Answers table
CREATE TABLE quiz_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    user_answer_json JSONB,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    answered_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_answers_attempt_id ON quiz_answers(attempt_id);
CREATE INDEX idx_quiz_answers_question_id ON quiz_answers(question_id);
