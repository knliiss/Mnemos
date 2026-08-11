FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY apps/reward-service/build/libs/reward-service.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
