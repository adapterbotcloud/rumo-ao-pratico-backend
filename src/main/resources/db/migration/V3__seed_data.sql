-- V3: Seed data - admin user, sample topics and questions
-- Password: admin123 hashed with BCrypt
INSERT INTO users (name, email, password_hash)
VALUES ('Admin', 'admin@rumo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Sample topics (user_id = 1 = admin)
INSERT INTO topics (user_id, name) VALUES (1, 'Arte Naval');
INSERT INTO topics (user_id, name) VALUES (1, 'Navegação Estimada e em Águas Restritas');
INSERT INTO topics (user_id, name) VALUES (1, 'Navegação Astronômica');
INSERT INTO topics (user_id, name) VALUES (1, 'Estabilidade');
INSERT INTO topics (user_id, name) VALUES (1, 'Miguens - Navegação');
INSERT INTO topics (user_id, name, parent_id) VALUES (1, 'Balizamento', 2);

-- Sample questions for Arte Naval (topic_id = 1)
INSERT INTO questions (user_id, topic_id, type, statement, explanation, bibliography, difficulty, tags, is_active)
VALUES (1, 1, 'MULTIPLE_CHOICE',
        'Qual é o tipo de proa que oferece melhor desempenho em mar agitado?',
        'A proa lançada oferece melhor desempenho em mar agitado por sua geometria que reduz o impacto das ondas.',
        'Bibliografia: Arte Naval, Fonseca, pg. 45',
        'MEDIUM',
        'proa,casco,hidrodinâmica',
        true);

INSERT INTO question_options (question_id, option_text, is_correct, explanation)
VALUES (1, 'Proa lançada', true, 'Correta - Geometria otimizada para águas agitadas');
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (1, 'Proa vertical', false);
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (1, 'Proa invertida', false);
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (1, 'Proa de colher', false);
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (1, 'Proa de bulbo', false);

-- Sample question for Navegação (topic_id = 2)
INSERT INTO questions (user_id, topic_id, type, statement, explanation, bibliography, difficulty, tags, is_active)
VALUES (1, 2, 'MULTIPLE_CHOICE',
        'Qual instrumento é utilizado para medir a profundidade do mar?',
        'O ecobatímetro utiliza ondas sonoras para medir a profundidade, sendo o instrumento principal para essa finalidade.',
        'Bibliografia: Miguens - Navegação, pg. 120',
        'EASY',
        'instrumentos,batimetria,profundidade',
        true);

INSERT INTO question_options (question_id, option_text, is_correct, explanation)
VALUES (2, 'Ecobatímetro', true, 'Correta - Utiliza ondas sonoras (sonar) para medir profundidade');
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (2, 'Anemômetro', false);
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (2, 'Barômetro', false);
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (2, 'Sextante', false);
INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (2, 'Goniômetro', false);

-- Sample TRUE_FALSE question
INSERT INTO questions (user_id, topic_id, type, statement, explanation, bibliography, difficulty, tags, is_active)
VALUES (1, 4, 'TRUE_FALSE',
        'O GM (altura metacêntrica) negativo indica que o navio está em condição de estabilidade positiva.',
        'GM negativo indica instabilidade - o navio tende a emborcar. Estabilidade positiva requer GM positivo.',
        'Bibliografia: Estabilidade, pg. 78',
        'EASY',
        'estabilidade,GM,metacentro',
        true);

INSERT INTO question_options (question_id, option_text, is_correct)
VALUES (3, 'Verdadeiro', false);
INSERT INTO question_options (question_id, option_text, is_correct, explanation)
VALUES (3, 'Falso', true, 'GM negativo = instabilidade');
