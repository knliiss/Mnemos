FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/account-service/build/libs/account-service.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]