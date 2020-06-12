FROM lucksoft/tomcat9-nginx-auth
ENV TZ Asia/Shanghai

WORKDIR /usr/local/tomcat

RUN rm -rf /usr/local/tomcat/webapps/*

ARG WAR_FILE

COPY server.xml /usr/local/tomcat/conf/

COPY assets/docker/nginx.conf /usr/local/nginx/conf/nginx.conf

COPY target/${WAR_FILE} /usr/local/tomcat/webapps/

COPY assets/dist /usr/share/nginx/html/

RUN mv /usr/local/tomcat/webapps/${WAR_FILE} /usr/local/tomcat/webapps/ROOT.war
