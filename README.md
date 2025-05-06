# Getting Started


1.Создайте ручками в постгре БД с названием card_db если постгря не в докер контейнере а установлена на ноут.

в консоле бд выполнить скрипт для инсерта админа в таблицу users:

BEGIN;


-- Шаг 1: Создаем пользователя admin (пароль: admin123)
WITH new_user AS (
INSERT INTO users (email, password)
VALUES (
'admin@example.com',
-- Пароль "admin123", захешированный с помощью bcrypt
'$2a$12$5gT7y5bR3WvGJjU5kq1ZzOQ7X9vD1VYfL3B7sN6wYdKc4rZzLm1O'
)
ON CONFLICT (email) DO NOTHING
RETURNING id
)

-- Шаг 3: Связываем пользователя с ролью
INSERT INTO user_roles (user_id, role_id)
SELECT
nu.id,
(SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
FROM new_user nu
WHERE NOT EXISTS (
SELECT 1
FROM user_roles ur
WHERE ur.user_id = nu.id
AND ur.role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);

-- Фиксируем изменения
COMMIT;

admin@example.com
Пароль admin123 захеширован с помощью алгоритма bcrypt (12 rounds)

Для генерации нового хеша можно использовать Online Bcrypt Generator



2. Документация будет доступна после запуска приложения:
Swagger UI: http://localhost:8080/swagger-ui.html
Raw JSON: http://localhost:8080/v3/api-docs
Сайты для визуализации документации:
   Swagger Editor (онлайн):
Сайт: https://editor.swagger.io/
Как использовать:
Загрузите сгенерированный openapi.json
Вставьте в левую панель
Интерактивная документация появится справа




Развертывание:
4. mvn clean package
5. Соберите Docker-образ: docker compose build
6. Запустите систему: docker compose up -d