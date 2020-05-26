FROM alpine/git as clone
WORKDIR /app
RUN git clone https://dgckero0263@dev.azure.com/dgckero0263/decision-manager/_git/decision-manager

FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw clean package -pl :decision-core,:decision-web -DskipTests -Dsonar.skip=true


FROM maven:3.6.1-jdk-8 as maven_builder

FROM tomcat:8.5.43-jdk8
COPY --from=maven_builder /decision-manager/decision-web/target/decision-web.war /usr/local/tomcat/webapps
