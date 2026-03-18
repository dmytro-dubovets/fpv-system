# Етап 1: Збірка (Maven)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Копіюємо pom.xml та сирці
COPY pom.xml .
COPY src ./src
# Збираємо jar без запуску тестів (щоб було швидше)
RUN mvn clean package -DskipTests

# Етап 2: Запуск (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Копіюємо зібраний jar з попереднього етапу
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]