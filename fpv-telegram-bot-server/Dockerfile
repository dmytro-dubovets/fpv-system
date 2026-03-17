# Етап 1: Використовуємо легкий образ JRE для запуску
FROM eclipse-temurin:21-jre-alpine

# Встановлюємо робочу директорію всередині контейнера
WORKDIR /app

# Додаємо корисний пакет для перевірки мережі (допомагає при дебазі зв'язку з серверами)
RUN apk add --no-cache curl

# Копіюємо JAR-файл, який Maven згенерував у папці target
# Використовуємо wildcard (*), щоб не залежати від точної версії в назві
COPY target/*.jar app.jar

# Налаштування JVM для Mac M4 та контейнерів (оптимізація пам'яті)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Запуск бота з прокиданням змінних оточення
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]