FROM govukpay/openjdk:8-jre-alpine


RUN apk update
RUN apk upgrade

RUN apk add bash

ENV JAVA_HOME /usr/lib/jvm/java-8-*/
ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

RUN apk add openssl && \
    mkdir -p bin && \
    apk del --purge openssl

ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/
ADD docker-startup.sh /app/docker-startup.sh
ADD cacerts /app/cacerts
ADD run-with-chamber.sh /app/run-with-chamber.sh

CMD bash ./docker-startup.sh
