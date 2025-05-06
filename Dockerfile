# Многоступенчатая сборка для уменьшения размера образа
FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

  # Финальный образ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

  # Копируем только нужные файлы
COPY --from=builder /app/target/*.jar app.jar

  # Настройки для оптимальной работы в контейнере
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
EXPOSE 8080

  # Запуск приложения с параметрами
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]