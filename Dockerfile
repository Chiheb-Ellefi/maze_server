
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY /target /app
EXPOSE 5000
CMD ["java", "-jar", "maze_final-1.0-SNAPSHOT.jar"]
