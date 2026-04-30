BEGIN TRANSACTION;

-- Додаємо тестового користувача
INSERT INTO users (username, email, password_hash)
VALUES ('student_test', 'test@knu.ua', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'); -- пароль: 123456

-- Додаємо категорії для цього користувача
INSERT INTO categories (user_id, name, description) VALUES
(1, 'Навчання', 'Матеріали для університету'),
(1, 'Програмування', 'Ресурси по Java та SQL');

-- Додаємо посилання
INSERT INTO links (category_id, url, title) VALUES
(1, 'https://classroom.google.com/', 'Google Classroom'),
(2, 'https://docs.oracle.com/en/java/', 'Java Official Documentation'),
(2, 'https://www.postgresql.org/docs/', 'PostgreSQL Docs');

-- Додаємо теги
INSERT INTO tags (name) VALUES
('Університет'), ('Java'), ('Бази Даних'), ('Довідник');

-- Зв'язуємо посилання з тегами (Багато до Багатьох)
-- Classroom -> Університет
INSERT INTO link_tags (link_id, tag_id) VALUES (1, 1);
-- Java Docs -> Java, Довідник
INSERT INTO link_tags (link_id, tag_id) VALUES (2, 2), (2, 4);
-- Postgres Docs -> Бази Даних, Довідник
INSERT INTO link_tags (link_id, tag_id) VALUES (3, 3), (3, 4);

COMMIT;