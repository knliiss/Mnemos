FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/moderation-service/build/libs/moderation-service.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
