FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/personalization-service/build/libs/personalization-service.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
