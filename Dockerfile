FROM eclipse-temurin:21-jre-alpine

VOLUME /tmp

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY target/*.jar cryptowallet.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "cryptowallet.jar"]