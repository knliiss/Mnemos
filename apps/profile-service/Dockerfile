FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/profile-service/build/libs/profile-service.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]
