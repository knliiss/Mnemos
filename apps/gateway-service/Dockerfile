FROM amazoncorretto:21-alpine

WORKDIR /app

COPY apps/gateway-service/build/libs/gateway-service.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
