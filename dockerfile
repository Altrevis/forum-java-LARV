FROM openjdk:11-jre-slim

WORKDIR /app

COPY . .


COPY src /app/src

COPY target /app/target

CMD [ "bash", "java /src/main/java/server/Server.java" ]

EXPOSE 8000