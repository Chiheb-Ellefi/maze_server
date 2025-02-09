
# Run stage
FROM openjdk:21-jdk-slim
WORKDIR /app
RUN mvn clean package
COPY --from=build /app/target/*.jar server.jar
EXPOSE 5000
CMD ["java", "-jar", "server.jar"]