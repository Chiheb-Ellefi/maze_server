# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package
# Verify the manifest
RUN echo "Verifying manifest..." && \
    jar tvf target/*.jar | grep MANIFEST && \
    unzip -p target/*.jar META-INF/MANIFEST.MF

# Run stage
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar server.jar
EXPOSE 5000
CMD ["java", "-jar", "server.jar"]