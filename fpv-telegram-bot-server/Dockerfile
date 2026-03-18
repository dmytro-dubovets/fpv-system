# Етап 1: Збірка (Maven)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Етап 2: Запуск (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Бот портів зазвичай не потребує, але можна лишити 8081 або прибрати EXPOSE
ENTRYPOINT ["java", "-jar", "app.jar"]