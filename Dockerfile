FROM maven:3.8-openjdk-21-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Run stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar server.jar
EXPOSE 5000
CMD ["java", "-jar", "server.jar"]