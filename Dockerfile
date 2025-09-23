FROM openjdk:17-jdk-alpine

WORKDIR  /petbookApp

COPY target/petbook-backend-0.0.1-SNAPSHOT.jar petbook.jar


EXPOSE 8080

CMD ["java", "-Xms256m", "-Xmx384m", "-jar", "petbook.jar"]
