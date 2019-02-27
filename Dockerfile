FROM maven:3.6.0-jdk-11-slim as build

RUN mkdir /build /root/.m2

ADD pom.xml /build/
ADD src /build/

WORKDIR /build/

RUN mvn clean package -DskipTests

FROM govukpay/openjdk:alpine-3.8.1-jre-base-8.191.12

RUN apk --no-cache upgrade

RUN apk add --no-cache bash

ENV JAVA_HOME /usr/lib/jvm/java-8-*/
ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD docker-startup.sh /app/docker-startup.sh
ADD run-with-chamber.sh /app/run-with-chamber.sh
#COPY --from=build /build/target/*.yaml /app/
COPY --from=build /build/target/pay-*-allinone.jar /app/

CMD bash ./docker-startup.sh
