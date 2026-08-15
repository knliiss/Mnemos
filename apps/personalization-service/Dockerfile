FROM amazoncorretto:21-alpine
WORKDIR /app
COPY build/libs/personalization-service.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
