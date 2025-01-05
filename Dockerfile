FROM openjdk:latest
ARG JAR_PATH
ARG EXPOSE_PORT
ARG JAR_FILE=${JAR_PATH}/build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE ${EXPOSE_PORT}
ENTRYPOINT ["java", "-Dspring.profiles.active=prod","-jar","/app.jar"]