FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/telegram-bot-service/build/libs/telegram-bot-service.jar app.jar

EXPOSE 8094

ENTRYPOINT ["java", "-jar", "app.jar"]
