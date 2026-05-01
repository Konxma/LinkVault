-- Таблиця 1: users (Користувачі)
-- Тип: Довідкова таблиця (Master table).
-- Нормалізація: 3НФ. Усі неключові атрибути (username, email, password_hash)
-- залежать тільки від первинного ключа (user_id) і не залежать один від одного.
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця 2: categories (Категорії/Папки)
-- Тип: Довідкова таблиця / Таблиця зв'язку 1:N (від users).
-- Нормалізація: 3НФ. Атрибути залежать лише від category_id.
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Таблиця 3: links (Посилання)
-- Тип: Транзакційна таблиця (Transaction table) - зберігає основні події системи.
-- Нормалізація: 3НФ. Дані специфічні лише для конкретного посилання.
CREATE TABLE links (
    link_id SERIAL PRIMARY KEY,
    category_id INTEGER REFERENCES categories(category_id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця 4: tags (Теги)
-- Тип: Словник (Dictionary table).
-- Нормалізація: 3НФ.
CREATE TABLE tags (
    tag_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Таблиця 5: link_tags (Зв'язок посилань та тегів)
-- Тип: Проміжна таблиця (Junction table). Реалізує зв'язок Багато-до-Багатьох (N:M).
-- Нормалізація: 3НФ. Первинний ключ є складеним.
CREATE TABLE link_tags (
    link_id INTEGER REFERENCES links(link_id) ON DELETE CASCADE,
    tag_id INTEGER REFERENCES tags(tag_id) ON DELETE CASCADE,
    PRIMARY KEY (link_id, tag_id)
);

-- Індекси для оптимізації пошуку (вимога викладача)
CREATE INDEX idx_links_category ON links(category_id);
CREATE INDEX idx_users_email ON users(email);