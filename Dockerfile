# Build Stage
FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y

WORKDIR /app
COPY . .

RUN ./gradlew clean stage --no-daemon

# Run Stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR files from the build stage
COPY --from=build /app/build/libs/*.jar ./

EXPOSE 8080
CMD ["java", "-jar", "topgenius-1.0.jar"]
