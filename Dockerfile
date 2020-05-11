
FROM tomcat:8.5.46

LABEL maintainer="dgckero@gmail.com"

ADD decision-web.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run"]
