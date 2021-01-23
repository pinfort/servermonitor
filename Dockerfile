FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle
WORKDIR /home/gradle
RUN gradle build

FROM openjdk:11.0-jre-slim

VOLUME /tmp

ARG JAR_FILE=/home/gradle/build/libs/servermonitor-0.0.1-SNAPSHOT.jar
COPY --from=builder ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
