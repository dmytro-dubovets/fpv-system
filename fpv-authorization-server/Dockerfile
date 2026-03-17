# Використовуємо образ Java 21
FROM eclipse-temurin:21-jre-alpine

# Робоча директорія
WORKDIR /app

# Копіюємо jar-файл (перевір точну назву у папці target!)
# Зазвичай вона така сама, як artifactId у pom.xml
COPY target/fpv-authorization-server-*.jar app.jar

# Відкриваємо порт 9000 (стандарт для Auth Server)
EXPOSE 9000

# Команда запуску
ENTRYPOINT ["java", "-jar", "app.jar"]