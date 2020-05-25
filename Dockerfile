FROM tomcat:8.5
MAINTAINER dgckero

RUN ["rm", "-rf", "/usr/local/tomcat/webapps/decision-web"]

#COPY ["/home/vsts/work/1/s/decision-web/target/decision-web.war","/usr/local/tomcat/webapps/"]

CMD ["catalina.sh", "run"]