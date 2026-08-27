FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/player-directory-service/build/libs/player-directory-service.jar app.jar

EXPOSE 8092

ENTRYPOINT ["java", "-jar", "app.jar"]
