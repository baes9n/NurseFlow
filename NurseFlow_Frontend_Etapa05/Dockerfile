# NurseFlow Frontend - compilacion Angular y servicio Nginx
FROM node:24-bookworm-slim AS build
WORKDIR /app
COPY package*.json ./
# El ZIP del examen no incluye package-lock.json; npm install genera uno dentro del build.
RUN npm install --no-audit --no-fund
COPY angular.json tsconfig*.json ./
COPY src ./src
RUN npm run build

FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/nurseflow-frontend/browser/ /usr/share/nginx/html/
EXPOSE 80
