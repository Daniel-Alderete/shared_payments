FROM ubuntu:20.04

# To launch mongo
# sudo docker network create mongo-network
# sudo docker run -d -p 27017:27017 --network mongo-network --name shared-payments-database mongo:latest

# To run this dockerfile, use (-d for detach)
# sudo docker build -t shared-payments-docker/backend .
# sudo docker run --network mongo-network -p 8080:8080 shared-payments-docker/backend

# -- Env vars
ENV MONGODB_VERSION_DEB=4.4
ENV MONGODB_VERSION=4.4.12
ENV DEBIAN_FRONTEND=noninteractive

# -- Update installed packages
RUN apt-get update && \
  apt-get dist-upgrade -y -o Dpkg::Options::='--force-confdef' -o Dpkg::Options::='--force-confold'

# -- Install Tools
RUN apt-get install -y default-jre git wget gnupg sudo unzip curl zip unzip unrar nano vim zsh iputils-ping apt-utils jq

# -- Install Mongo
RUN curl -fsSL https://www.mongodb.org/static/pgp/server-$MONGODB_VERSION_DEB.asc | sudo apt-key add -
RUN echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/$MONGODB_VERSION_DEB multiverse" | \
  sudo tee /etc/apt/sources.list.d/mongodb-org-$MONGODB_VERSION_DEB.list
RUN apt-get update && \
  apt-get install -y --allow-unauthenticated mongodb-org=$MONGODB_VERSION mongodb-org-server=$MONGODB_VERSION \
  mongodb-org-shell=$MONGODB_VERSION mongodb-org-mongos=$MONGODB_VERSION mongodb-org-tools=$MONGODB_VERSION
RUN apt-get install -y libc6-dev

# -- delete local artifacts
RUN rm -rf ~/.m2/repository/com/practice/shared_payment_backend

# -- Home and permissions setup
USER root
RUN sudo mkdir -p /data/db
RUN sudo chown -R mongodb: /data/db
RUN sudo chmod -R 777 /data/db

RUN rm -rf /tmp/*
RUN apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# -- Launch Spring app
RUN useradd -ms /bin/bash backenduser
USER backenduser
RUN mkdir -p ~/logs
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]