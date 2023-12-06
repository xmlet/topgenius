FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y

COPY . .

RUN ./gradlew clean stage --no-daemon

FROM openjdk:17-jdk-slim

COPY --from=build /build/libs/topgenius-1.0.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
