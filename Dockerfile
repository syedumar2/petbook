# Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/target/petbook-backend-0.0.1-SNAPSHOT.jar petbook-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms256m", "-Xmx384m", "-jar", "petbook-backend.jar"]
