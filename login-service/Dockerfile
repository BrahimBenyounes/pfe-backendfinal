
FROM maven:3.6-openjdk-17 AS builder

COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn -q -Dmaven.test.skip=true -f /home/app/pom.xml clean package


FROM openjdk:17-alpine


COPY target/*.jar login-service.jar

ENTRYPOINT ["java", "-jar", "/login-service.jar"]


