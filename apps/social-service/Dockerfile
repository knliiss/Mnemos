FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/social-service/build/libs/social-service.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
