FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/bot-management-service/build/libs/bot-management-service.jar app.jar

EXPOSE 8095

ENTRYPOINT ["java", "-jar", "app.jar"]
