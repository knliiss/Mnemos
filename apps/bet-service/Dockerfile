FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/bet-service/build/libs/bet-service.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
