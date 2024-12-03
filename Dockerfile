FROM openjdk:17-jdk-slim

WORKDIR /app

ARG REDIS_HOST
ARG REDIS_PW

ENV REDIS_HOST=${REDIS_HOST}
ENV REDIS_PW=${REDIS_PW}

COPY build/libs/camp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]