FROM alpine/git as clone
WORKDIR /decision-manager
RUN git clone https://dgckero0263@dev.azure.com/dgckero0263/decision-manager/_git/decision-manager

FROM maven:3.6.1-jdk-8 as maven_builder

WORKDIR /decision-manager
ADD decision-web/pom.xml $HOME/decision-web

RUN ["mvn","clean","package","-DskipTests=true -Dsonar.skip=true"]

ADD . $HOME

RUN ["/usr/local/bin/mvn-entrypoint.sh", "mvn", "verify", "clean", "--fail-never"]

FROM tomcat:8.5.43-jdk8
COPY --from=maven_builder /decision-manager/decision-web/target/decision-web.war /usr/local/tomcat/webapps
