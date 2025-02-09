# Stage 1: Build the application using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace/app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final image with the built JAR file
FROM openjdk:21
WORKDIR /app
COPY --from=build /workspace/app/target/maze_final-1.0-SNAPSHOT.jar /app/server.jar
EXPOSE 5000
CMD ["java", "-jar", "server.jar"]