# 1. Build Stage (Updated to Java 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the JAR file, skipping tests to save time
RUN mvn clean package -DskipTests

# 2. Run Stage (Updated to Java 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]