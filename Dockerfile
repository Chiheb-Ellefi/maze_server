
FROM openjdk:21-jdk-slim
WORKDIR /app
CMD ["mvn","clean","package"]
COPY /target/maze_final-1.0-SNAPSHOT.jar /app/server.jar
EXPOSE 5000
CMD ["java", "-jar", "server.jar"]
