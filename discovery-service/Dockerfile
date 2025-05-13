
# مرحلة البناء
FROM maven:3.6-openjdk-17 AS builder

COPY src /home/app/src
COPY pom.xml /home/app


RUN mvn -q -Dmaven.test.skip=true -f /home/app/pom.xml clean package


FROM openjdk:17-alpine


COPY target/*.jar discovery-service.jar

# تشغيل التطبيق
ENTRYPOINT ["java", "-jar", "/discovery-service.jar"]
