FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY apps/payment-service/build/libs/payment-service.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
