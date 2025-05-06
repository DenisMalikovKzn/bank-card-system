# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.5/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.5/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.5/reference/web/servlet.html)
* [Spring Security](https://docs.spring.io/spring-boot/3.4.5/reference/web/spring-security.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.4.5/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Liquibase Migration](https://docs.spring.io/spring-boot/3.4.5/how-to/data-initialization.html#howto.data-initialization.migration-tool.liquibase)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.



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

2.# Генерация ключа
openssl rand -base64 32
# Сохранить вывод в encryption.secret-key

3. Документация будет доступна после запуска приложения:
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