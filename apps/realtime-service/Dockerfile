FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/realtime-service/build/libs/realtime-service.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
