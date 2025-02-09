
FROM openjdk:21-jdk-slim
WORKDIR /app
RUN mvn clean package
COPY /target/*.jar /app/server.jar
EXPOSE 5000
CMD ["java", "-jar", "server.jar"]
