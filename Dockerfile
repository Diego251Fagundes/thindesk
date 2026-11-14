FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B package spring-boot:repackage

FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /build/target/thindesk-*.war app.war
ENV SPRING_PROFILES_ACTIVE=render
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60.0", "-jar", "app.war"]
