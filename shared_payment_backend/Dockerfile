FROM openjdk:8-jdk-alpine
# In order to deploy this api in a container, follow these steps
# mvn clean package
# sudo docker-compose build
# sudo docker-compose up

LABEL maintainer="daniel.alderetecillero@gmail.com"
VOLUME /shared-payments-backend
RUN addgroup -S sharedpayments && adduser -S backenduser -G sharedpayments
USER backenduser
RUN mkdir -p ~/logs
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]