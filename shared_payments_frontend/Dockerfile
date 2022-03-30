# To create the docker image 
# sudo docker build -t shared-payments-frontend/dockerize-vuejs-app .

# To run the application
# sudo docker run -it -p 8081:80 --rm --name dockerize-vuejs-app-1 shared-payments-frontend/dockerize-vuejs-app

# Compilation
FROM node:17-alpine3.14 as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install 
COPY . .
RUN npm run build

# Production
FROM nginx:1.13.12-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]