# Використовуємо легкий образ Java
FROM eclipse-temurin:21-jre-alpine

# Вказуємо робочу директорію всередині контейнера
WORKDIR /app

# Копіюємо зібраний jar-файл з папки target (після mvn clean package)
# Перевір, щоб назва файлу збігалася з тією, що в папці target!
COPY target/fpv-resource-server-*.jar app.jar

# Відкриваємо порт 8080
EXPOSE 8080

# Команда для запуску
ENTRYPOINT ["java", "-jar", "app.jar"]