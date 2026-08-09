FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/wallet-service/build/libs/wallet-service.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
