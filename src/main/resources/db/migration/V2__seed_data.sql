-- Seed data: demo user (password: "password123" bcrypt-hashed)
INSERT INTO users (id, name, email, password_hash)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Demo User',
    'demo@rumoaopratico.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
);

-- Seed topics
INSERT INTO topics (id, user_id, name, description) VALUES
('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Direito Constitucional', 'Questões de Direito Constitucional'),
('b2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Direito Civil', 'Questões de Direito Civil'),
('b3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Direito Penal', 'Questões de Direito Penal');

-- Sub-topics
INSERT INTO topics (id, user_id, name, parent_id, description) VALUES
('b4eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Direitos Fundamentais', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Direitos e Garantias Fundamentais'),
('b5eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Organização do Estado', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Organização Político-Administrativa');

-- Seed questions
INSERT INTO questions (id, user_id, topic_id, type, statement, explanation, bibliography, difficulty, tags) VALUES
('c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
 'MULTIPLE_CHOICE',
 'Qual é o fundamento da República Federativa do Brasil que se refere à dignidade da pessoa humana?',
 'A dignidade da pessoa humana é um dos fundamentos da República Federativa do Brasil, conforme Art. 1º, III da CF/88.',
 'Constituição Federal de 1988, Art. 1º',
 'EASY',
 'constituição,fundamentos,república'),

('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
 'TRUE_FALSE',
 'O Brasil adota a forma federativa de Estado, que é cláusula pétrea.',
 'Correto. A forma federativa de Estado é cláusula pétrea conforme Art. 60, §4º, I da CF/88.',
 'Constituição Federal de 1988, Art. 60',
 'MEDIUM',
 'constituição,cláusula pétrea'),

('c3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
 'MULTIPLE_CHOICE',
 'Sobre a capacidade civil, é correto afirmar:',
 'A capacidade civil plena é adquirida aos 18 anos conforme Art. 5º do Código Civil.',
 'Código Civil, Art. 5º',
 'MEDIUM',
 'capacidade civil,código civil');

-- Seed question options
INSERT INTO question_options (id, question_id, text, is_correct, order_index) VALUES
('d1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Art. 1º, inciso III da Constituição Federal', TRUE, 0),
('d2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Art. 5º, inciso I da Constituição Federal', FALSE, 1),
('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Art. 3º, inciso II da Constituição Federal', FALSE, 2),
('d4eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Art. 4º, inciso IV da Constituição Federal', FALSE, 3);

-- TRUE_FALSE options
INSERT INTO question_options (id, question_id, text, is_correct, order_index) VALUES
('d5eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Verdadeiro', TRUE, 0),
('d6eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Falso', FALSE, 1);

-- More MULTIPLE_CHOICE options
INSERT INTO question_options (id, question_id, text, is_correct, order_index) VALUES
('d7eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'A capacidade civil plena é adquirida aos 18 anos', TRUE, 0),
('d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'A capacidade civil plena é adquirida aos 16 anos', FALSE, 1),
('d9eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'A capacidade civil plena é adquirida aos 21 anos', FALSE, 2),
('da0ebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'A capacidade civil independe de idade', FALSE, 3);

-- Seed a quiz attempt
INSERT INTO quiz_attempts (id, user_id, started_at, finished_at, total_questions, correct_count, mode, config_json) VALUES
('e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
 NOW() - INTERVAL '1 hour', NOW() - INTERVAL '30 minutes', 3, 2, 'STUDY',
 '{"topicIds": ["b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"], "difficulty": "MEDIUM"}');

-- Seed quiz answers
INSERT INTO quiz_answers (id, attempt_id, question_id, user_answer_json, is_correct, answered_at) VALUES
('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
 '{"selectedOptionId": "d1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}', TRUE, NOW() - INTERVAL '50 minutes'),
('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
 '{"selectedOptionId": "d5eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}', TRUE, NOW() - INTERVAL '45 minutes'),
('f3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
 '{"selectedOptionId": "d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}', FALSE, NOW() - INTERVAL '40 minutes');
