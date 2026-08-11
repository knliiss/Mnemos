FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY apps/event-service/build/libs/event-service.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]