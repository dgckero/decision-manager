FROM alpine/git as clone
WORKDIR /decision-manager
RUN git clone https://dgckero0263@dev.azure.com/dgckero0263/decision-manager/_git/decision-manager

FROM maven:3.6.1-jdk-8 as maven_builder

WORKDIR /decision-manager
ADD pom.xml $HOME

RUN ["/usr/local/bin/mvn-entrypoint.sh", "mvn", "verify", "clean", "--fail-never"]

ADD . $HOME

RUN ["mvn","clean","package","-pl :decision-core,:decision-web","-DskipTests=true -Dsonar.skip=true"]

FROM tomcat:8.5.43-jdk8
COPY --from=maven_builder /decision-manager/decision-web/target/decision-web.war /usr/local/tomcat/webapps
