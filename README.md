# Shared Payments
### Description
An application that calculates the shared payments for a group of friends. 
It has been implemented with Spring boot for the backend and Vue JS for the frontend.
The backend is an API REST that allows CRUD operations over a set of models, and is used to retrieve information about the shared payments.
A full specification can be found in the swagger provided.
The frontend is a SPA that displays this information, among other things new groups of friends, friends and payments can be created or selected.

Spring Boot configuration can be done in `application.properties`

Logger configuration is done via logback, all settings can be modified in `logback.xml` file

### Data Model
This application is built upon the relationship between three data models:
- A Payment, that contains its amount, description and date
- A Friend, that contains its name, surname, and a list of payments
- A Group, that contains its name, description and a list of friends

### Running the app
Both backend and frontend applications were built using docker containers. They can be launched using the following commands

**For the backend app:**

Generate a new package of the server
`mvn clean package`

Build the image
`sudo docker-compose build`

Run the image
`sudo docker-compose up`

**For the frontend app:**

Build the image
`sudo docker build -t shared-payments-frontend/dockerize-vuejs-app .`

Run the image
`sudo docker run -it -p 8081:80 --rm --name dockerize-vuejs-app-1 shared-payments-frontend/dockerize-vuejs-app`

### Technical Specification
- Java 1.8.0
- Spring Boot 2.6.5 - https://spring.io/projects/spring-boot
- Logback 1.2.10 - https://logback.qos.ch
- MongoDB 4.4.3 - https://www.mongodb.com/
- Vue JS 3 - https://vuejs.org/
- Moment JS - https://momentjs.com/
- Docker - https://www.docker.com/
