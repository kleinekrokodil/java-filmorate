-- 1. Отключаем проверку внешних ключей
SET REFERENTIAL_INTEGRITY FALSE;

-- 2. Очистка БД
TRUNCATE TABLE film_likes;
TRUNCATE TABLE user_friends;
TRUNCATE TABLE mpa RESTART IDENTITY;
TRUNCATE TABLE film_genres RESTART IDENTITY;
TRUNCATE TABLE genres RESTART IDENTITY;
TRUNCATE TABLE films RESTART IDENTITY;
TRUNCATE TABLE users RESTART IDENTITY;

-- 3. Включаем проверку обратно
SET REFERENTIAL_INTEGRITY TRUE;

-- Добавление рейтингов
MERGE INTO mpa (id, name) KEY(id) VALUES
        (1, 'G'),
        (2, 'PG'),
        (3, 'PG-13'),
        (4, 'R'),
        (5, 'NC-17');

-- Добавление жанров
MERGE INTO genres (id, name) KEY(id) VALUES
        (1, 'Комедия'),
        (2, 'Драма'),
        (3, 'Мультфильм'),
        (4, 'Триллер'),
        (5, 'Документальный'),
        (6, 'Боевик');

