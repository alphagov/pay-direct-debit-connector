FROM adoptopenjdk/openjdk11:jdk-11.0.2.9-alpine

RUN apk --no-cache upgrade

# openssl is only here temporarily whilst docker-startup.sh needs it
RUN apk add --no-cache bash openssl

ENV JAVA_HOME /usr/lib/jvm/java-8-*/
ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD docker-startup.sh /app/docker-startup.sh
ADD run-with-chamber.sh /app/run-with-chamber.sh
ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/

CMD bash ./docker-startup.sh
